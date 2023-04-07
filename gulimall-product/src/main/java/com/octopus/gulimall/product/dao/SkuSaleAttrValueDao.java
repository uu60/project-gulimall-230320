package com.octopus.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.octopus.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.octopus.gulimall.product.vo.SkuItemSaleAttrVo;
import com.octopus.gulimall.product.vo.SkuItemVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 14:20:10
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttrVo> getSaleAttrBySpuId(Long spuId);

    List<String> getSkuSaleAttrValuesAsStringList(Long skuId);
}
