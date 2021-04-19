package com.stylefeng.guns.order.modular.order.dao;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.order.vo.OrderVo;
import com.stylefeng.guns.order.modular.order.model.CcOrder;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 订单信息表 Mapper 接口
 * </p>
 *
 * @author Ashen
 * @since 2020-10-30
 */
public interface CcOrderMapper extends BaseMapper<CcOrder> {
    // 根据场次信息查询座位信息
    String getSeatsByFieldId(@Param("fieldId") String fieldId);

    // 根据订单id查询订单详情
    OrderVo getOrderInfoById(@Param("orderId") String orderId);

    // 查询用户的订单
    List<OrderVo> getOrdersByUserId(@Param("userId") Integer userId, Page<OrderVo> page);

    // 根据放映场次查询已售座位
    String getSoldSeatsByFieldId(@Param("fieldId") Integer fieldId);
}