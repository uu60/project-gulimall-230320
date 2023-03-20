package com.octopus.gulimall.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.octopus.gulimall.coupon.entity.CouponEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 15:25:59
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
