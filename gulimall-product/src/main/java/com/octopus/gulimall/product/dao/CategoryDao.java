package com.octopus.gulimall.product.dao;

import com.octopus.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author octopus
 * @email djz6660@icloud.com
 * @date 2023-03-20 01:34:58
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
