package com.octopus.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopus.common.constant.ProductConstant;
import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.Query;
import com.octopus.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.octopus.gulimall.product.dao.AttrDao;
import com.octopus.gulimall.product.dao.AttrGroupDao;
import com.octopus.gulimall.product.dao.CategoryDao;
import com.octopus.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.octopus.gulimall.product.entity.AttrEntity;
import com.octopus.gulimall.product.entity.AttrGroupEntity;
import com.octopus.gulimall.product.entity.CategoryEntity;
import com.octopus.gulimall.product.service.AttrService;
import com.octopus.gulimall.product.service.CategoryService;
import com.octopus.gulimall.product.vo.AttrGroupRelationVo;
import com.octopus.gulimall.product.vo.AttrRespVo;
import com.octopus.gulimall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVo attr) {
        // 先保存属性
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attr, entity);
        this.save(entity);

        // 保存关联关系
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(entity.getAttrId());
            relationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryAttrPage(Map<String, Object> params, String attrType, Long categoryId) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type",
                "base".equalsIgnoreCase(attrType) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() :
                        ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if (categoryId != 0) {
            queryWrapper.eq("category_id", categoryId);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);

        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> respVos = records.stream().map(entity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(entity, attrRespVo);

            // 设置分类和分组的名字
            if ("base".equalsIgnoreCase(attrType)) {
                AttrAttrgroupRelationEntity relationEntity =
                        relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",
                                entity.getAttrId()));
                if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity =
                            attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryDao.selectById(entity.getCategoryId());
            if (categoryEntity != null) {
                attrRespVo.setCategoryName(categoryEntity.getName());
            }

            return attrRespVo;
        }).collect(Collectors.toList());

        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    @Cacheable(value = "attr", key = "'attrInfo:' + #root.args[0]")
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        AttrAttrgroupRelationEntity relationEntity =
                relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",
                        attrId));

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 设置分组信息
            if (relationEntity != null) {
                Long attrGroupId = relationEntity.getAttrGroupId();
                attrRespVo.setAttrGroupId(attrGroupId);

                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                if (attrGroupEntity != null) {
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        // 设置分类信息
        Long categoryId = attrEntity.getCategoryId();
        Long[] categoryPath = categoryService.findCategoryPath(categoryId);
        attrRespVo.setCategoryPath(categoryPath);

        return attrRespVo;
    }

    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attr, entity);
        this.updateById(entity);

        // 修改分组信息
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());

            Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq(
                    "attr_id", attr.getAttrId())).intValue();
            if (count == 0) {
                relationDao.insert(attrAttrgroupRelationEntity);
            } else {
                relationDao.update(attrAttrgroupRelationEntity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            }
        }
    }

    /**
     * 根据分组id查找关联的所有基本属性
     *
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> entities =
                relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id",
                        attrgroupId));
        List<Long> collect = entities.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return null;
        }
        List<AttrEntity> attrEntities = this.listByIds(collect);
        return attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> collect = Arrays.asList(vos).stream().map(item -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(collect);
    }

    /**
     * 获取当前分组没有关联的所有属性
     *
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        // 当前分组只能关联自己所属分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long categoryId = attrGroupEntity.getCategoryId();
        // 当前分组只能关联别的分组没有引用的分组
        // 1 当前分类下的其他分组
        List<AttrGroupEntity> groups = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq(
                "category_id", categoryId));
        List<Long> groupIds = groups.stream().map(group -> group.getAttrGroupId()).collect(Collectors.toList());
        // 2 这些分组的关联属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("category_id",
                categoryId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        List<AttrAttrgroupRelationEntity> relationEntities =
                relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        List<Long> attrIds =
                relationEntities.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());
        if (!attrIds.isEmpty()) {
            // 从当前分类的所有属性中移除这些属性
            queryWrapper.notIn("attr_id", attrIds);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        return baseMapper.selectSearchAttrIds(attrIds);
    }


}