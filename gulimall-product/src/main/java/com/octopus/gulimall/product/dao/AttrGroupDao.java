package com.octopus.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.octopus.gulimall.product.entity.AttrGroupEntity;
import com.octopus.gulimall.product.vo.SkuItemVo;
import com.octopus.gulimall.product.vo.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 14:20:10
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    // TODO: p205-209
    @SuppressWarnings("all")
    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("categoryId") Long categoryId);

}
