package com.ashen.xunwu.service.user.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.ashen.xunwu.service.ServiceResult;
import com.ashen.xunwu.service.user.ISmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务
 */
@Service
@Slf4j
public class SmsServiceImpl implements ISmsService, InitializingBean {

    @Value("${aliyun.sms.accessKey}")
    private String accessKey;
    @Value("${aliyun.sms.accessKeySecret}")
    private String secertKey;
    @Value("${aliyun.sms.template.code}")
    private String templateCode;
    @Value("${aliyun.sms.sign.name}")
    private String signName;
    
    private IAcsClient acsClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final static String SMS_CODE_CONTENT_PREFIX = "SMS::CODE::CONTENT::";

    /**
     * 发送短信验证码
     * @param telephone
     * @return
     */
    @Override
    public ServiceResult<String> sendSms(String telephone) {
        // 验证缓存是否失效
        String gapKey = "SMS::CODE::INTERVAL::" + telephone;
        String result = redisTemplate.opsForValue().get(gapKey);
        if (result != null) {
            return new ServiceResult<>(false, "请求次数太频繁");
        }

        // 生成验证码
        String code = this.generateRandomSmsCode();

        //组装请求对象
        SendSmsRequest request = new SendSmsRequest();
        request.setSysMethod(MethodType.POST);  //使用post提交
        request.setPhoneNumbers(telephone);     //待发送手机号
        request.setSignName(signName);          //短信签名
        request.setTemplateCode(templateCode);  //短信模板
        request.setTemplateParam("{\"code\":\"" + code + "\"}");   //模板中的变量替换JSON串

        //请求失败这里会抛ClientException异常
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (ClientException e) {
            log.error("Error to send sms code to {}", telephone);
        }
        
        if(sendSmsResponse != null && "OK".equals(sendSmsResponse.getCode())) {
            // 设置请求时间限制 60s，控制请求频率
            redisTemplate.opsForValue().set(gapKey, code, 60, TimeUnit.SECONDS);
            // 缓存验证码
            redisTemplate.opsForValue().set(SMS_CODE_CONTENT_PREFIX + telephone, code, 5, TimeUnit.MINUTES);
            return ServiceResult.of(code);
        } else {
            return new ServiceResult<>(false, "服务忙，请稍后重试");
        }
    }

    /**
     * 生成验证码
     * @return
     */
    private String generateRandomSmsCode() {
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            str.append(random.nextInt(10));
        }
        return str.toString();
    }

    @Override
    public String getSmsCode(String telephone) {
        

        return null;
    }

    @Override
    public void remove(String telephone) {

    }

    /**
     * 初始化阿里云客户端
     * @throws ClientException
     */
    @Override
    public void afterPropertiesSet() throws ClientException {
        // 设置超时时间-可自行调整
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        // 初始化ascClient需要的几个参数
        final String product = "Dysmsapi";                      // 短信API产品名称（短信产品名固定，无需修改）
        final String domain = "dysmsapi.aliyuncs.com";          // 短信API产品域名（接口地址固定，无需修改）
        // 设置AccessKey
        final String accessKeyId = "yourAccessKeyId";           //accessKeyId
        final String accessKeySecret = "yourAccessKeySecret";   //accessKeySecret
        // 初始化ascClient,暂时不支持多region（请勿修改）
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        acsClient = new DefaultAcsClient(profile);
    }
    
}
