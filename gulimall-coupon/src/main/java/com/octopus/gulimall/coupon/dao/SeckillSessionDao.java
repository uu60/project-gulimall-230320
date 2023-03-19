package com.octopus.gulimall.coupon.dao;

import com.octopus.gulimall.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author octopus
 * @email djz6660@icloud.com
 * @date 2023-03-20 02:08:49
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}
