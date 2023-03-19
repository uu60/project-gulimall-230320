package com.octopus.gulimall.member.dao;

import com.octopus.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author octopus
 * @email djz6660@icloud.com
 * @date 2023-03-20 02:12:12
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
