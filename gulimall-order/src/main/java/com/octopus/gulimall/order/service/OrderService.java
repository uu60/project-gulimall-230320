package com.octopus.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.octopus.common.utils.PageUtils;
import com.octopus.gulimall.order.entity.OrderEntity;
import com.octopus.gulimall.order.vo.OrderConfirmVo;
import com.octopus.gulimall.order.vo.OrderSubmitVo;
import com.octopus.gulimall.order.vo.SubmitOrderResponseVo;

import java.util.Map;

/**
 * 订单
 *
 * @author octopus
 * @email djz6660@icloud.com
 * @date 2023-03-20 02:10:19
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);
}

