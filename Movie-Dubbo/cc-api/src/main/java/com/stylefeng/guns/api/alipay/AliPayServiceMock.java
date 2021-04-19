package com.stylefeng.guns.api.alipay;

import com.stylefeng.guns.api.alipay.vo.AliPayInfoVo;
import com.stylefeng.guns.api.alipay.vo.AliPayResultVo;

/**
 * 业务降级实现，实现AliPayService接口，方法实现全是降级方法
 * 
 * Dubbo本地伪装类，需要实现和真正服务相同的接口
 * 当配置了本地伪装，访问不到服务提供方或访问超时，会访问本地伪装的相应方法
 * 
 * 根据Dubbo文档，mock只能捕获 RpcException，所以上面的访问不到和超时可转发到mock服务
 */
public class AliPayServiceMock implements AliPayService {
    
    @Override
    public AliPayInfoVo getQRCode(String orderId) {
        return null;
    }

    @Override
    public AliPayResultVo getOrderStatus(String orderId) {
        AliPayResultVo aliPayResultVo = new AliPayResultVo();
        aliPayResultVo.setOrderId(orderId);
        aliPayResultVo.setOrderStatus(0);
        aliPayResultVo.setOrderMsg("尚未支付成功");
        return aliPayResultVo;
    }
}
