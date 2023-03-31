package com.octopus.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.octopus.common.utils.PageUtils;
import com.octopus.gulimall.product.entity.AttrGroupEntity;
import com.octopus.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.octopus.gulimall.product.vo.SkuItemVo;
import com.octopus.gulimall.product.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 14:20:10
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long categoryId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCategoryId(Long categoryId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long categoryId);
}

