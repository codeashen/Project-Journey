package com.ashen.xunwu.web.dto;

import lombok.Data;

/**
 * 七牛上传结果类
 */
@Data
public class QiNiuPutRet {
    public String key;
    public String hash;
    public String bucket;
    public int width;
    public int height;
}
