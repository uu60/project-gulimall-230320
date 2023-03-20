package com.octopus.gulimall.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.octopus.gulimall.member.entity.MemberLoginLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员登录记录
 * 
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 15:35:45
 */
@Mapper
public interface MemberLoginLogDao extends BaseMapper<MemberLoginLogEntity> {
	
}
