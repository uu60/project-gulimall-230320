package com.octopus.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.octopus.common.utils.PageUtils;
import com.octopus.gulimall.order.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author octopus
 * @email djz6660@icloud.com
 * @date 2023-03-20 02:10:19
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

