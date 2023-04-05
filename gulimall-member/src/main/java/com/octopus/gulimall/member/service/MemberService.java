package com.octopus.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.octopus.common.utils.PageUtils;
import com.octopus.gulimall.member.entity.MemberEntity;
import com.octopus.gulimall.member.exception.PhoneExistedException;
import com.octopus.gulimall.member.exception.UsernameExistedException;
import com.octopus.gulimall.member.vo.MemberLoginVo;
import com.octopus.gulimall.member.vo.MemberRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 15:35:45
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo memberRegisterVo);

    void checkUsernameUnique(String username) throws UsernameExistedException;
    void checkPhoneUnique(String phone) throws PhoneExistedException;

    MemberEntity login(MemberLoginVo memberLoginVo);
}

