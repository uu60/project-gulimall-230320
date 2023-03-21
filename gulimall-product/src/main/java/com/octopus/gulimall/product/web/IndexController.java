package com.octopus.gulimall.product.web;

import com.octopus.gulimall.product.entity.CategoryEntity;
import com.octopus.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author octopus
 * @date 2023/3/21 17:46
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        List<CategoryEntity> level1Categories = categoryService.getLevel1Categories();

        model.addAttribute("categorys", level1Categories); .

        return "index";
    }
}
