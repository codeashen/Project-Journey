package com.stylefeng.guns.order;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stylefeng.guns.order.util.FTPUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CcOrderApplicationTests {
    
    @Autowired
    private FTPUtil ftpUtil;

	@Test
	public void testFTPUtil() {
        String json = ftpUtil.getFileStrByAddress("seats/cgs.json");
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray ids = (JSONArray) jsonObject.get("ids");
        String[] arr = {"1", "2"};
        List<Integer> collect = Arrays.stream(arr).map(Integer::parseInt).collect(Collectors.toList());
        boolean flag = ids.containsAll(collect);
        System.out.println(json);
	}

}
