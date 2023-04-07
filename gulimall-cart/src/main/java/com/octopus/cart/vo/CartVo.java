package com.octopus.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author octopus
 * @date 2023/4/7 17:29
 */
@Data
public class Cart {

    private List<CartItem> items;
    private Integer countNum;
    /**
     * 商品类型数量
     */
    private Integer countType;
    /**
     * 购物项总价
     */
    private BigDecimal totalAmount;
    /**
     * 减免价格
     */
    private BigDecimal reduce;

    public void updateCountNum() {
        int count = 0;
        if (items == null || items.size() == 0) {
            for (CartItem item : items) {
                count++;
            }
        }
        countNum = count;
    }
}
