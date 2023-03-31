package com.octopus.search.service;

import com.octopus.search.to.SearchParam;
import com.octopus.search.to.SearchResult;

/**
 * @author octopus
 * @date 2023/3/22 17:44
 */
public interface MallSearchService {


    SearchResult search(SearchParam param);
}
