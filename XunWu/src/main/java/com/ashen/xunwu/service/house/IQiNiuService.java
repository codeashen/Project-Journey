package com.ashen.xunwu.service.house;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

import java.io.File;
import java.io.InputStream;

/**
 * 七牛云服务
 */
public interface IQiNiuService {
    // 文件上传
    Response uploadFile(File file) throws QiniuException;
    // 数据流上传
    Response uploadFile(InputStream inputStream) throws QiniuException;
    // 删除文件
    Response delete(String key) throws QiniuException;
}
