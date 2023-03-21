package com.octopus.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.Query;
import com.octopus.gulimall.product.dao.AttrGroupDao;
import com.octopus.gulimall.product.entity.AttrEntity;
import com.octopus.gulimall.product.entity.AttrGroupEntity;
import com.octopus.gulimall.product.service.AttrGroupService;
import com.octopus.gulimall.product.service.AttrService;
import com.octopus.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.octopus.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long categoryId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (categoryId != 0) {
            wrapper.eq("category_id", categoryId);
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    /**
     * 根据分类id查出所有的分组以及这些组里面的属性
     * @param categoryId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCategoryId(Long categoryId) {
        List<AttrGroupEntity> entities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", categoryId));
        List<AttrGroupWithAttrsVo> collect = entities.stream().map(entity -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(entity, attrGroupWithAttrsVo);
            List<AttrEntity> attrEntities = attrService.getRelationAttr(attrGroupWithAttrsVo.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(attrEntities);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long categoryId) {
        // 1. 查出当前spu对应的所有属性的分组信息以及当前分组下的属性对应值
        List<SkuItemVo.SpuItemAttrGroupVo> vos = this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId, categoryId);
        return null;
    }


}