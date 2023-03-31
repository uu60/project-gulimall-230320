package com.octopus.gulimall.product.feign;

import com.octopus.common.utils.R;
import com.octopus.gulimall.product.fallback.SeckillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author octopus
 * @date 2023/3/31 17:55
 */
//@FeignClient(value = "gulimall-seckill",fallback = SeckillFeignServiceFallBack.class)
public interface SeckillFeignService {

    /**
     * 根据skuId查询商品是否参加秒杀活动
     * @param skuId
     * @return
     */
    @GetMapping(value = "/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);

}
