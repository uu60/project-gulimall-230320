package com.octopus.search.service;

import com.octopus.common.to.SkuEsModel;

import java.util.List;

/**
 * @author octopus
 * @date 2023/3/21 15:36
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModelList);
}
