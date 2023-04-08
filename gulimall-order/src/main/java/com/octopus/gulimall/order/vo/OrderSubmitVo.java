package com.octopus.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author octopus
 * @date 2023/4/8 19:24
 * @description 订单提交信息
 */
@Data
public class OrderSubmitVo {
    /** 收获地址的id **/
    private Long addrId;

    /** 支付方式 **/
    private Integer payType;
    //无需提交要购买的商品，去购物车再获取一遍
    //优惠、发票

    /** 防重令牌 **/
    private String orderToken;

    /** 应付价格 **/
    private BigDecimal payPrice;

    /** 订单备注 **/
    private String remarks;

    //用户相关的信息，直接去session中取出即可
}
