package com.octopus.gulimall.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.octopus.gulimall.coupon.entity.CouponSpuRelationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券与产品关联
 * 
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 15:26:00
 */
@Mapper
public interface CouponSpuRelationDao extends BaseMapper<CouponSpuRelationEntity> {
	
}
