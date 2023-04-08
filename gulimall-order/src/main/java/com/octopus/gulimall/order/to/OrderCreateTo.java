package com.octopus.gulimall.order.to;

import com.octopus.gulimall.order.entity.OrderEntity;
import com.octopus.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author octopus
 * @date 2023/4/8 19:45
 */
@Data
public class OrderCreateTo {
    private OrderEntity order;  // 订单
    private List<OrderItemEntity> orderItems; // 订单项
    /** 订单计算的应付价格 **/
    private BigDecimal payPrice;
    /** 运费 **/
    private BigDecimal fare;
}
