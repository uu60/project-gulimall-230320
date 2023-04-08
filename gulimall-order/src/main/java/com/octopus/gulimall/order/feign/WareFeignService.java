package com.octopus.gulimall.order.feign;

import com.octopus.common.utils.R;
import com.octopus.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author octopus
 * @date 2023/4/8 18:36
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    @GetMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @PostMapping("ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
