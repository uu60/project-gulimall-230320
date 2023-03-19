package com.octopus.gulimall.coupon.dao;

import com.octopus.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author octopus
 * @email djz6660@icloud.com
 * @date 2023-03-20 02:08:49
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
