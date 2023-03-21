package com.octopus.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrRespVo extends AttrVo {
    private String categoryName;
    private String groupName;
    private Long[] categoryPath;
}
