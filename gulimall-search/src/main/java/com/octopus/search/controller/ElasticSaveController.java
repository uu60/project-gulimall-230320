package com.octopus.search.controller;

import com.octopus.common.exception.BizCodeEnum;
import com.octopus.common.to.SkuEsModel;
import com.octopus.common.utils.R;
import com.octopus.search.service.ProductSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author octopus
 * @date 2023/3/21 15:34
 */
@RestController
@RequestMapping("/search/save")
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    // 上架商品
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList) {
        boolean b = productSaveService.productStatusUp(skuEsModelList);
        return b ? R.ok() : R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),
                BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
    }
}
