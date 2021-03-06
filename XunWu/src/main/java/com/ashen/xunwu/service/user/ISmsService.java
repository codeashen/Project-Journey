package com.ashen.xunwu.service.user;

import com.ashen.xunwu.service.ServiceResult;

/**
 * 短信验证码服务
 */
public interface ISmsService {
    /**
     * 发送验证码到指定手机 并 缓存验证码 5分钟 及 请求间隔时间1分钟
     * @param telephone
     * @return
     */
    ServiceResult<String> sendSms(String telephone);

    /**
     * 获取缓存中的验证码
     * @param telephone
     * @return
     */
    String getSmsCode(String telephone);

    /**
     * 移除指定手机号的验证码缓存
     */
    void remove(String telephone);
}
