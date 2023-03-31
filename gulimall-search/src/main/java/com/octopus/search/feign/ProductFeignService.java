package com.octopus.search.feign;

import com.octopus.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author octopus
 * @date 2023/3/22 20:17
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {


    @RequestMapping("/info/{brandId}")
    R brandsInfo(List<Long> brandId);

    @RequestMapping("/info/{attrId}")
    R attrInfo(long parseLong);
}
