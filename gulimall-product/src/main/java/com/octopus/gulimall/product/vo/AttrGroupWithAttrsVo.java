package com.octopus.gulimall.product.vo;

import com.octopus.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupWithAttrsVo {
    private Long attrGroupId;
    private String attrGroupName;
    private Integer sort;
    private String descript;
    private String icon;
    private Long catalogId;
    private List<AttrEntity> attrs;
}
