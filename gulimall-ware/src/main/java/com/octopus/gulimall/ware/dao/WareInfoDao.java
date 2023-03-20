package com.octopus.gulimall.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.octopus.gulimall.ware.entity.WareInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库信息
 * 
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 15:39:28
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {
	
}
