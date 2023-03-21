package com.octopus.gulimall.ware.feign;

import com.octopus.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    /**
     * feign可以写成两种
     * 1 直接请求服务
     * 2 经过网关
     * @param skuId
     * @return
     */
    @RequestMapping("product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
