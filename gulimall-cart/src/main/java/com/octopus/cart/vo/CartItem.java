package com.octopus.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author octopus
 * @date 2023/4/7 17:30
 */
@Data
public class CartItem {

    private Long skuId;
    private Boolean check;
    private String title;
    private String image;
    private List<String> akuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;

    public void updateTotalPrice() {
        totalPrice = price.multiply(BigDecimal.valueOf(count));
    }
}
