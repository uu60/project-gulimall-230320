package com.octopus.search.to;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {
    private String keyword;//页面传递过来的全文匹配关键字 v
    private Long category3Id;//三级分类 id v
    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;//排序条件


    /**
     * 过滤条件
     *  hasStock(是否有货)、 skuPrice 区间、 brandId、 category3Id、 attrs
     *  hasStock=0/1
     *  skuPrice=1_500/_500/500_
     *  brandId=1
     *  attrs=2_5 存:6 寸
     */

    private Integer hasStock;//是否只显示有货  0（无库存） 1（有库存）
    private String skuPrice;//价格区间查询
    private List<Long> brandId;//按照品牌进行查询， 可以多选
    private List<String> attrs;//按照属性进行筛选
    private Integer pageNum = 1;//页码
    private String _queryString;//原生的所有查询条件
}
