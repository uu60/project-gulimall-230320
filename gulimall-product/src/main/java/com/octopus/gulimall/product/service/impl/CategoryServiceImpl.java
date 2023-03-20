package com.octopus.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.Query;
import com.octopus.gulimall.product.dao.CategoryDao;
import com.octopus.gulimall.product.entity.CategoryEntity;
import com.octopus.gulimall.product.service.CategoryBrandRelationService;
import com.octopus.gulimall.product.service.CategoryService;
import com.octopus.gulimall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 组装成父子的树形结构
        return getAllEntitiesByTree(entities);
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO: 检查当前菜单，是否被别的地方引用

        // 逻辑删除
        // 1-application.yml中配置逻辑删除规则（省略）
        // 2-配置逻辑删除组件Bean（省略）
        // 3-给Bean加上逻辑删除注解
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatalogPath(Long catalogId) {
        if (catalogId == null) {
            return new Long[0];
        }
        List<Long> path = new ArrayList<>();
        findParentPath(catalogId, path);
        return path.toArray(new Long[0]);
    }

    @Override
    // Spring事务框架默认只在抛出RuntimeException和unchecked exceptions时才将事务回滚（Errors默认 - 事务回滚），但是抛出的Checked
    // exceptions时将不进行事务回滚。
    @Transactional(rollbackFor = Exception.class)
//    @CacheEvict(value = "category", key = "'getLevel1Categories'")
    @Caching(evict = {
            @CacheEvict(value = "category", key = "'getLevel1Categories'"),
            @CacheEvict(value = "category", key = "'getCatalogJson'")
    })
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    // 每一个需要缓存的数据我们都要来指定放到哪个名字下【缓存的分区（按照业务类型）】
    @Cacheable(value = {"category"}, key = "#root.method.name")
    // 代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
    // 如果缓存中没有，会调用方法，最后将方法的结果放入缓存
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Override
    @Cacheable(value = "category", key = "#root.methodName")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        // 1 查出所有一级分类
        List<CategoryEntity> level1Categories = getChildren(selectList, 0L);
        // 2 封装数据
        return level1Categories.stream().collect(Collectors.toMap(e -> e.getCatId().toString(), e1 -> {
            // 1. 每一个一级分类，查到这个一级分类所有二级分类
            List<CategoryEntity> categoryEntities = getChildren(selectList, e1.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(e2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(e1.getCatId().toString(), null,
                            e2.getCatId().toString(),
                            e2.getName());
                    // 查询三级
                    List<CategoryEntity> e3s = getChildren(selectList, e2.getCatId());
                    List<Object> vo3s = null;
                    if (e3s != null) {
                        // 封装成指定格式
                        vo3s = e3s.stream().map(e3 -> {
                            return new Catalog2Vo.Catalog3Vo(e2.getCatId().toString(), e3.getCatId().toString(),
                                    e3.getName());
                        }).collect(Collectors.toList());
                    }
                    catalog2Vo.setCatalog3List(vo3s);
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
    }


    private Map<String, List<Catalog2Vo>> getCatalogJsonFromDb() {
    /*
      将数据库的多次查询变为一次
     */
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        // 1 查出所有一级分类
        List<CategoryEntity> level1Categories = getChildren(selectList, 0L);
        // 2 封装数据
        Map<String, List<Catalog2Vo>> collect =
                level1Categories.stream().collect(Collectors.toMap(e -> e.getCatId().toString(), e1 -> {
                    // 1. 每一个一级分类，查到这个一级分类所有二级分类
                    List<CategoryEntity> categoryEntities = getChildren(selectList, e1.getCatId());
                    List<Catalog2Vo> catalog2Vos = null;
                    if (categoryEntities != null) {
                        catalog2Vos = categoryEntities.stream().map(e2 -> {
                            Catalog2Vo catalog2Vo = new Catalog2Vo(e1.getCatId().toString(), null,
                                    e2.getCatId().toString(),
                                    e2.getName());
                            // 查询三级
                            List<CategoryEntity> e3s = getChildren(selectList, e2.getCatId());
                            List<Object> vo3s = null;
                            if (e3s != null) {
                                // 封装成指定格式
                                vo3s = e3s.stream().map(e3 -> {
                                    return new Catalog2Vo.Catalog3Vo(e2.getCatId().toString(), e3.getCatId().toString(),
                                            e3.getName());
                                }).collect(Collectors.toList());
                            }
                            catalog2Vo.setCatalog3List(vo3s);
                            return catalog2Vo;
                        }).collect(Collectors.toList());
                    }
                    return catalog2Vos;
                }));
        // 查到的数据再放入缓存，将查出的对象转为JSON
//        redisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(collect), 1, TimeUnit.DAYS);
        return collect;
    }

    private List<CategoryEntity> getChildren(List<CategoryEntity> selectList, Long parentCid) {
        return selectList.stream().filter(entity -> entity.getParentCid().equals(parentCid)).collect(Collectors.toList());
    }

    private void findParentPath(Long catalogId, List<Long> path) {
        CategoryEntity entityById = this.getById(catalogId);
        path.add(catalogId);
        Long parentCid = entityById.getParentCid();
        if (parentCid == 0) {
            Collections.reverse(path);
            return;
        }
        findParentPath(parentCid, path);
    }

    private List<CategoryEntity> getAllEntitiesByTree(List<CategoryEntity> all) {
        return getChildren(null, all);
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(entity -> entity.getParentCid() == (root == null ? 0 : root.getCatId())).map(category -> {
            category.setChildren(getChildren(category, all));
            return category;
        }).sorted(Comparator.comparingInt(m -> (m.getSort() == null ? 0 : m.getSort()))).collect(Collectors.toList());
    }
}