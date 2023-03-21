package com.octopus.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopus.common.constant.ProductConstant;
import com.octopus.common.to.SkuEsModel;
import com.octopus.common.to.SkuReductionTo;
import com.octopus.common.to.SpuBoundTo;
import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.Query;
import com.octopus.common.utils.R;
import com.octopus.gulimall.product.dao.SpuInfoDao;
import com.octopus.gulimall.product.entity.*;
import com.octopus.gulimall.product.feign.CouponFeignService;
import com.octopus.gulimall.product.feign.SearchFrignService;
import com.octopus.gulimall.product.feign.WareFeignService;
import com.octopus.gulimall.product.service.*;
import com.octopus.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private SearchFrignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1 保存spu基本信息 pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        // 2 保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity infoDescEntity = new SpuInfoDescEntity();
        infoDescEntity.setSpuId(infoEntity.getId());
        infoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(infoDescEntity);

        // 3 保存Spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(infoEntity.getId(), images);

        // 4 保存Spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(attrEntity.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(collect);

        // 5 保存Spu的积分信息 gulimall_sms -> sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        // 6 保存当前Spu对应的所有Sku信息
        List<Skus> skus = vo.getSkus();
        if (skus != null && !skus.isEmpty()) {
            skus.forEach(sku -> {
                // 6.1 Sku的基本信息 pms_sku_info
                String defaultImg = "";
                List<Images> defaultImgList =
                        sku.getImages().stream().filter(img -> img.getDefaultImg() == 1).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(defaultImgList)) {
                    defaultImg = defaultImgList.get(0).getImgUrl();
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCategoryId(infoEntity.getCategoryId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.save(skuInfoEntity);

                // 6.2 Sku的图片信息 pms_sku_images
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = sku.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> StringUtils.hasText(entity.getImgUrl())).collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);

                // 6.3 Sku的销售属性信息 pms_sku_sale_attr_value
                List<Attr> attrList = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrList.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);

                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 6.4 Sku的优惠、满减等信息 gulimall_sms -> sms_sku_ladder\sms_sku_full_reduction\sms_spu_bounds
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存spu优惠信息失败");
                    }
                }
            });
        }


    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            wrapper.and(w -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if (StringUtils.hasText(status)) {
            wrapper.or().eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.hasText(brandId) && !"0".equals(brandId)) {
            wrapper.or().eq("brand_id", brandId);
        }
        String categoryId = (String) params.get("categoryId");
        if (StringUtils.hasText(categoryId) && !"0".equals(categoryId)) {
            wrapper.or().eq("category_id", categoryId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 4 查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attr> attrList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            BeanUtils.copyProperties(item, attr);
            return attr;
        }).collect(Collectors.toList());


        // 查出当前spuid对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skuInfoEntities.stream().map(entity -> {
            return entity.getSkuId();
        }).collect(Collectors.toList());
        Map<Long, Boolean> stockMap = null;
        try {
            // 发送远程调用，库存系统查询是否有库存
            R skusHasStocks = wareFeignService.getSkusHasStock(skuIds);
            List<Map<Object, Object>> data = (List<Map<Object, Object>>) skusHasStocks.get("data");
            stockMap = data.stream().collect(Collectors.toMap(m -> Long.parseLong(m.get("skuId").toString()), m -> (Boolean) m.get("hasStock")));
        } catch (Exception e) {
            log.error("远程库存查询异常：原因{}", e);
        }
        // 封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skuInfoEntities.stream().map(sku -> {
            // 组装我们需要的数据
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);
            // skuPrice, skuImg, hasStack, hotScore
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            if (finalStockMap != null) {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            } else {
                esModel.setHasStock(true);
            }
            // 热度评分
            esModel.setHotScore(0L);

            // 3 查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(esModel.getCategoryId());
            esModel.setCategoryName(categoryEntity.getName());

            // 设置检索属性
            esModel.setAttrs(attrList);

            return esModel;
        }).collect(Collectors.toList());

        // 5 将信息发送给es进行保存
        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0) {
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // TODO: 重复调用？接口幂等性；重试机制
        }
    }
}