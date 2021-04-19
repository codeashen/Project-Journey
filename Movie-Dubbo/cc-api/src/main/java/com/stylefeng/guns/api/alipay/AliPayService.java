package com.stylefeng.guns.api.alipay;

import com.stylefeng.guns.api.alipay.vo.AliPayInfoVo;
import com.stylefeng.guns.api.alipay.vo.AliPayResultVo;
import org.mengyun.tcctransaction.api.Compensable;

public interface AliPayService {

    /**
     * 获取支付二维码
     * @param orderId
     * @return
     */
    AliPayInfoVo getQRCode(String orderId);

    /**
     * 获取订单支付状态
     * @param orderId
     * @return
     */
    @Compensable
    AliPayResultVo getOrderStatus(String orderId);

}
