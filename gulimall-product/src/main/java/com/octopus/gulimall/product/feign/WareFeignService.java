package com.octopus.gulimall.product.feign;

import com.octopus.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    @PostMapping("ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
