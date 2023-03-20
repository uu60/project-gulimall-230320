package com.octopus.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.octopus.common.utils.PageUtils;
import com.octopus.gulimall.coupon.entity.SeckillPromotionEntity;

import java.util.Map;

/**
 * 秒杀活动
 *
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 15:26:00
 */
public interface SeckillPromotionService extends IService<SeckillPromotionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

