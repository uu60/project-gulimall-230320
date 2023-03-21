package com.octopus.search.config.controller;

import com.octopus.common.to.SkuEsModel;
import com.octopus.common.utils.R;
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
@RequestMapping("/search")
public class ElasticSaveController {

    // 上架商品
    @PostMapping
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList) {
        
    }
}
