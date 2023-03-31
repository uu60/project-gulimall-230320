package com.octopus.search.controller;

import com.octopus.search.service.MallSearchService;
import com.octopus.search.to.SearchParam;
import com.octopus.search.to.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author octopus
 * @date 2023/3/22 17:37
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model) {
        SearchResult searchResult = mallSearchService.search(param);

        model.addAttribute("result", searchResult);
        return "list";
    }
}
