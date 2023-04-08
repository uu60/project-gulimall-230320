package com.octopus.gulimall.order.vo;

import lombok.Data;

/**
 * @author octopus
 * @date 2023/4/8 18:41
 */
@Data
public class SkuStockVo {
    private Long skuId;
    private Boolean hasStock;
}
