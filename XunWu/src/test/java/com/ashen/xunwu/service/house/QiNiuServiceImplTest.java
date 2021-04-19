package com.ashen.xunwu.service.house;

import com.ashen.xunwu.ApplicationTests;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

class QiNiuServiceImplTest extends ApplicationTests {

    @Autowired
    private IQiNiuService qiNiuService;

    @Test
    void uploadFile() throws QiniuException {
        String fileName = "D:\\IdeaProjects\\xunwu\\tmp\\20201124102644.png";
        File file = new File(fileName);
        Response response = qiNiuService.uploadFile(file);
        System.out.println(response);
    }

    @Test
    void testUploadFile() throws FileNotFoundException, QiniuException {
        FileInputStream inputStream = new FileInputStream("D:\\IdeaProjects\\xunwu\\tmp\\20201124102644.png");
        Response response = qiNiuService.uploadFile(inputStream);
        System.out.println(response);
    }

    @Test
    void delete() throws QiniuException {
        Response response = qiNiuService.delete("Fnl3sK7oPomNKkTvQqvTK4JCigXr");
        System.out.println(response);
    }
}