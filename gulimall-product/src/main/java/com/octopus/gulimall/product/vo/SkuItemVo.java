package com.octopus.gulimall.product.vo;

import com.octopus.gulimall.product.entity.SkuImagesEntity;
import com.octopus.gulimall.product.entity.SkuInfoEntity;
import com.octopus.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    // 1. sku基本信息获取 pms_sku_info
    SkuInfoEntity Info;
    // 2. sku的图片信息 pms_sku_images
    List<SkuImagesEntity> images;
    // 3. 获取spu的销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;
    // 4. 获取spu的介绍
    SpuInfoDescEntity desp;
    // 5. 获取spu规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    @Data
    public static class SkuItemSaleAttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }

    @Data
    public static class SpuItemAttrGroupVo {
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }

    @Data
    public static class SpuBaseAttrVo {
        private String attrName;
        private String attrValue;
    }
}
