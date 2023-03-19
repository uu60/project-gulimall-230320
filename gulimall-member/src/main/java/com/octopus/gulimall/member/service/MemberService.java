package com.octopus.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.octopus.common.utils.PageUtils;
import com.octopus.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author octopus
 * @email djz6660@icloud.com
 * @date 2023-03-20 02:12:12
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

