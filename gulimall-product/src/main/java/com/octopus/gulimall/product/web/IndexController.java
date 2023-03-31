package com.octopus.gulimall.product.web;

import com.octopus.gulimall.product.entity.CategoryEntity;
import com.octopus.gulimall.product.service.CategoryService;
import com.octopus.gulimall.product.vo.Category2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

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

        model.addAttribute("categorys", level1Categories);

        return "index";
    }

    @GetMapping("/index/category.json")
    @ResponseBody
    public Map<String, List<Category2Vo>> getCategoryJson() {
        return categoryService.getCategoryJson();
    }
}
