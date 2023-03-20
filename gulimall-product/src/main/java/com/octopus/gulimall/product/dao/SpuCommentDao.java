package com.octopus.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.octopus.gulimall.product.entity.SpuCommentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价
 * 
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 14:20:10
 */
@Mapper
public interface SpuCommentDao extends BaseMapper<SpuCommentEntity> {
	
}
