package com.octopus.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author octopus
 * @date 2023/4/8 02:45
 */
@Controller
public class IndexController {

    @GetMapping("/{page}.html")
    public String index(@PathVariable String page) {
        return page;
    }
}
