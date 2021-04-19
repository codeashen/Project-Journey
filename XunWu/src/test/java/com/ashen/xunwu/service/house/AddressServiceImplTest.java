package com.ashen.xunwu.service.house;

import com.ashen.xunwu.ApplicationTests;
import com.ashen.xunwu.service.ServiceResult;
import com.ashen.xunwu.service.search.common.BaiduMapLocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AddressServiceImplTest extends ApplicationTests {
    
    @Autowired
    private IAddressService addressService;

    @Test
    void getBaiduMapLocation() {
        ServiceResult<BaiduMapLocation> location = addressService.getBaiduMapLocation("北京市", "北京市海淀区上地十街10号");
        BaiduMapLocation result = location.getResult();
        System.out.println(result);
    }
}