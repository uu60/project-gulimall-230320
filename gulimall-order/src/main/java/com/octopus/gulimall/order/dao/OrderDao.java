package com.octopus.gulimall.order.dao;

import com.octopus.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author octopus
 * @email djz6660@icloud.com
 * @date 2023-03-20 02:10:19
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
