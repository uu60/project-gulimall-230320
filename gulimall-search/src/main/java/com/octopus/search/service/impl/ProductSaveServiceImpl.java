package com.octopus.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.octopus.common.to.SkuEsModel;
import com.octopus.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.octopus.search.config.GulimallElasticSearchConfig.COMMON_OPTIONS;

/**
 * @author octopus
 * @date 2023/3/21 15:37
 */
@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient esTemplate;

    /**
     * 上架商品
     *
     * @param skuEsModelList 所有要上架的sku
     * @return
     */
    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModelList) {
        // 防止多次请求，使用bulk
        BulkRequest bulkRequest = new BulkRequest("product");
        for (SkuEsModel skuEsModel : skuEsModelList) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.id(skuEsModel.getSkuId().toString())
                    .source(JSON.toJSONString(skuEsModel), XContentType.JSON);

            bulkRequest.add(indexRequest);
        }

        try {
            BulkResponse bulk = esTemplate.bulk(bulkRequest, COMMON_OPTIONS);
            // 如果批量错误
            if (bulk.hasFailures()) {
                log.error("商品上架错误: {}",
                        Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList()));
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
