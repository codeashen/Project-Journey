package com.stylefeng.guns.alipay;

import com.stylefeng.guns.alipay.modular.alipay.util.FTPUtil;
import com.stylefeng.guns.alipay.AlipayApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AlipayApplication.class)
public class FTPUtilTest {
    @Autowired
    private FTPUtil ftpUtil;

    @Test
    public void contextLoads() {
        String fileStrByAddress = ftpUtil.getFileStrByAddress("seats/cgs.json");
        File file = new File("C:\\Users\\wanggaosheng\\Desktop\\A537212982A476F81A8A24E284708F97EE3D95D0_size1866_w397_h190.gif");
        boolean b = ftpUtil.uploadFile("target_file.gif", file);
        System.out.println("上传是否成功 = " + b);
        System.out.println(fileStrByAddress);

    }
}
