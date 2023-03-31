package com.octopus.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author octopus
 * @date 2023/3/31 18:24
 */
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<SpuBaseAttrVo> attrs;
}
