package com.stylefeng.guns.api.order.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.order.vo.OrderStatusEnum;
import com.stylefeng.guns.api.order.vo.OrderVo;

import java.util.List;

public interface OrderService {
    // 验证购买的票是否存在
    boolean isTrueSeats(String fieldId, String seats);

    // 已经出售的座位里，有没有这些座位
    boolean isNotSoldSeats(String fieldId, String seats);

    // 创建订单信息
    OrderVo saveOrderInfo(Integer fieldId, String soldSeats, String seatsName, Integer userId);

    // 查询当前登陆人已经购买的订单
    List<OrderVo> getOrderByUserId(Integer userId, Page<OrderVo> page);

    // 根据FieldId 获取所有已出售的座位编号
    String getSoldSeatsByFieldId(Integer fieldId);

    // 根据订单编号获取订单信息
    OrderVo getOrderInfoById(String orderId);

    // 更新订单支付状态
    boolean updateOrderStatus(String orderId, OrderStatusEnum status);

}
