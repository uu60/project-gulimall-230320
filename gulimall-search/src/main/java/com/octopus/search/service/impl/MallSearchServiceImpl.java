package com.octopus.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.octopus.common.to.SkuEsModel;
import com.octopus.common.utils.R;
import com.octopus.search.constant.Constant;
import com.octopus.search.feign.ProductFeignService;
import com.octopus.search.service.MallSearchService;
import com.octopus.search.to.AttrResponseVo;
import com.octopus.search.to.SearchParam;
import com.octopus.search.to.SearchResult;
import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.octopus.search.config.GulimallElasticSearchConfig.COMMON_OPTIONS;

/**
 * @author octopus
 * @date 2023/3/22 17:44
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient esTemplate;
    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        /*
            查询语句
            GET product/_search
            {
                "query": {
                    "bool": {
                        "must": [{
                            "match": {
                                "skuTitle": "xiaomi"
                            }
                        }],
                        "filter": [{
                            "term": {
                                "categoryId": 123
                            }
                        }, {
                            "terms": {
                                "brandId": [1, 2, 9]
                        }]
                        ......
                    }
                }
            }
         */
        SearchRequest searchRequest = buildRequest(param);
        try {
            SearchResponse response = esTemplate.search(searchRequest, COMMON_OPTIONS);
            SearchResult result = buildResult(param, response);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SearchRequest buildRequest(SearchParam param) {
        SearchRequest ret = new SearchRequest("product");

        SearchSourceBuilder builder = new SearchSourceBuilder();

        // 构建bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 1. must模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        // 2. bool - filter
        if (!StringUtils.isEmpty(param.getCategory3Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId", param.getCategory3Id()));
        }
        // 3. brand id
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 4. 指定属性
        /*

        "nested": {
            "path": "attrs",
            "query": {
                "bool": {
                    "must": [
                        {
                            "term": {
                                "attrs.attrId": {
                                    "value": "15"
                                }
                            }
                        },
                        {
                            "terms": {
                                "attrs.attrValue": [
                                    "海思",
                                    "以官网信息为准"
                                ]
                            }
                        }
                    }
                }
            }
        }
         */
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attr : param.getAttrs()) {
                // 每一个都要生成一个nested查询
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                // attr=1_5寸:8寸
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }
        }

        // 5. stock
        if (param.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        // 6. 价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");

            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (param.getSkuPrice().startsWith("_")) {
                rangeQuery.lte(s[0]);
            } else {
                rangeQuery.gte(s[0]);
            }

            boolQueryBuilder.filter(rangeQuery);
        }

        builder.query(boolQueryBuilder);

        // 排序，分页，高亮
        // 1. 排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            // sort=hotScore_asc
            String[] s = sort.split("_");
            SortOrder sortOrder = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            builder.sort(s[0], sortOrder);
        }
        // 2. 分页
        int pageSize = 2;
        builder.from((param.getPageNum() - 1) * pageSize);
        builder.size(pageSize);
        // 3. 高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder().field("skuTitle").preTags("<b style='color: " +
                    "red'>").postTags("</b>");
            builder.highlighter(highlightBuilder);
        }

        // 聚合分析
        builder.aggregation(AggregationBuilders.terms("brand_agg")
                        .field("brandId")
                        .size(50)
                        .subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName")
                                .size(1))
                        .subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg")
                                .size(1)))
                .aggregation(AggregationBuilders.terms("category_agg")
                        .field("categoryId")
                        .size(50)
                        .subAggregation(AggregationBuilders.terms("category_name_agg").field("categoryName").size(1))
                )
                .aggregation(AggregationBuilders.nested("attr_agg", "attrs")
                        .subAggregation(AggregationBuilders.terms("attr_id_agg").field("attrs.attrId")
                                .subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1))
                                .subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50)))
                );

        ret.source(builder);

        return ret;
    }

    private SearchResult buildResult(SearchParam param, SearchResponse response) {
        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();
        // 1、返回所有查询到的商品
        List<SkuEsModel> products = new ArrayList<>();
        if (!ArrayUtils.isEmpty(hits.getHits())) {
            for (SearchHit hit : hits.getHits()) {
                String jsonStr = hit.getSourceAsString();
                SkuEsModel model = JSON.parseObject(jsonStr, SkuEsModel.class);
                // 设置高亮信息
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    model.setSkuTitle(skuTitle.getFragments()[0].string());
                }
                products.add(model);
            }
        }
        result.setProducts(products);

        // 2、当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<? extends Terms.Bucket> attrBuckets = attr_id_agg.getBuckets();
        for (Terms.Bucket bucket : attrBuckets) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 提取属性ID
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
            // 提取属性名字
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attr_name_agg.getBuckets().get(0).getKeyAsString());
            // 提取品牌图片
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attr_value_agg.getBuckets().stream().map(item -> {
                return ((Terms.Bucket) item).getKeyAsString();
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);


        // 3、当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<? extends Terms.Bucket> brandBuckets = brand_agg.getBuckets();
        for (Terms.Bucket bucket : brandBuckets) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 提取品牌ID
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            // 提取品牌名字
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            brandVo.setBrandName(brand_name_agg.getBuckets().get(0).getKeyAsString());
            // 提取品牌图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            brandVo.setBrandImg(brand_img_agg.getBuckets().get(0).getKeyAsString());
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // 4、当前所有商品涉及到的所有分类信息
        List<SearchResult.CategoryVo> categoryVos = new ArrayList<>();
        ParsedLongTerms category_agg = response.getAggregations().get("category_agg");
        List<? extends Terms.Bucket> categoryBuckets = category_agg.getBuckets();
        for (Terms.Bucket bucket : categoryBuckets) {
            SearchResult.CategoryVo categoryVo = new SearchResult.CategoryVo();
            // 提取分类Id
            categoryVo.setCategoryId(Long.parseLong(bucket.getKeyAsString()));
            // 提取分类名字
            ParsedStringTerms category_name_agg = bucket.getAggregations().get("category_name_agg");
            categoryVo.setCategoryName(category_name_agg.getBuckets().get(0).getKeyAsString());
            categoryVos.add(categoryVo);
        }
        result.setCategorys(categoryVos);
        // 5、分页信息   pageNum:当前页码 、total:总记录数 、totalPages: 总页码
        long total = hits.getTotalHits().value;
        int totalPages = (int) total % Constant.PRODUCT_PAGE_SIZE == 0 ? (int) total / Constant.PRODUCT_PAGE_SIZE :
                ((int) total / Constant.PRODUCT_PAGE_SIZE + 1);
        result.setPageNum(param.getPageNum());
        result.setTotal(total);
        result.setTotalPages(totalPages);

        // 6、所有页码数
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);


        // 7、构建面包屑导航功能
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                //1、分析每一个attrs传过来的参数值
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                // attrs=2_5寸:6寸
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                // 根据请求构造面包屑 规格属性Id集合，这个集合包含的属性规格不显示【前端会遍历每个参数显示】
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    // 设置属性名
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }

                //2、取消了这个面包屑以后，我们要跳转到哪个地方，将请求的地址url里面的当前置空
                //拿到所有的查询条件，去掉当前
                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());

            result.setNavs(collect);
        }

        // 品牌、分类
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            // TODO 远程查询所有品牌
            R r = productFeignService.brandsInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<SearchResult.BrandVo> brand = r.getData("brand", new TypeReference<List<SearchResult.BrandVo>>() {
                });
                StringBuffer sb = new StringBuffer();
                String replace = "";
                for (SearchResult.BrandVo brandVo : brand) {
                    sb.append(brandVo.getBrandName() + ";");
                    replace = replaceQueryString(param, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(sb.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }


        // TODO 分类 面包屑
        return result;
    }

    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+", "%20");  //浏览器对空格的编码和Java不一样，差异化处理
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 就是点了X之后，应该跳转的地址
        // 这里要判断一下，attrs是不是第一个参数，因为第一个参数 没有&符号
        // TODO BUG，第一个参数不带&
        return param.get_queryString().replace("&" + key + "=" + encode, "");
    }
}
