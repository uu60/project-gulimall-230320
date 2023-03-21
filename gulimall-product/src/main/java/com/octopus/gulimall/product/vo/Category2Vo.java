package com.octopus.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category2Vo {
    private String category1Id; // 1级父节点id
    private List<Object> category3List; // 3级子分类
    private String id;
    private String name;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Category3Vo {
        private String category2Id;
        private String id;
        private String name;
    }
}
