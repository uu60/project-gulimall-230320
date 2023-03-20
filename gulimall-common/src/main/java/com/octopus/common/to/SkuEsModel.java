package com.octopus.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuEsModel {
    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    private Long hotScore;
    private Long brandId;
    private Long catalogId;
    private String catalogName;
    private String brandName;
    private String brandImg;
    private String catalogImg;
    private List<Attr> attrs;
    private Boolean hasStock;

    @Data
    public static class Attr {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
