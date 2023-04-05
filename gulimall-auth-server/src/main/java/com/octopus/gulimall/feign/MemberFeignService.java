package com.octopus.gulimall.feign;

import com.octopus.common.utils.R;
import com.octopus.gulimall.vo.UserLoginVo;
import com.octopus.gulimall.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author octopus
 * @date 2023/3/31 20:41
 */
@FeignClient(name = "gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo memberRegisterVo);

    @PostMapping("/member/member/login")
    R login(UserLoginVo vo);
}
