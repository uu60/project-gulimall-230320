package com.octopus.gulimall.ware.feign;

import com.octopus.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author octopus
 * @date 2023/4/9 18:01
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable String orderSn);
}
