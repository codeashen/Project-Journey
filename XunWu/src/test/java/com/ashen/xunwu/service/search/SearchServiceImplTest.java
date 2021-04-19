package com.ashen.xunwu.service.search;

import com.ashen.xunwu.ApplicationTests;
import com.ashen.xunwu.service.ServiceMultiResult;
import com.ashen.xunwu.service.ServiceResult;
import com.ashen.xunwu.web.form.RentSearch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Assertions;

import java.util.List;

/**
 * ES业务测试
 */
class SearchServiceImplTest extends ApplicationTests {
    
    @Autowired
    private ISearchService searchService;

    @Test
    void index() throws Exception {
        Long houseId = 25L;
        searchService.index(houseId);
    }

    @Test
    void remove() {
    }

    @Test
    void query() {
        RentSearch rentSearch = new RentSearch();
        rentSearch.setCityEnName("bj");
        rentSearch.setStart(0);
        rentSearch.setSize(10);
        rentSearch.setKeywords("国贸");
        ServiceMultiResult<Long> serviceResult = searchService.query(rentSearch);

        Assertions.assertTrue(serviceResult.getTotal() > 0);
    }
    
    @Test
    void suggest() {
        ServiceResult<List<String>> list = searchService.suggest("大");
        System.out.println(list);
    }
    
    @Test
    void aggregateDistrictHouse() {
        ServiceResult<Long> result = searchService.aggregateDistrictHouse("bj", "hdq", "融泽嘉园");
        Assertions.assertTrue(result.getResult() > 0);
    }
}