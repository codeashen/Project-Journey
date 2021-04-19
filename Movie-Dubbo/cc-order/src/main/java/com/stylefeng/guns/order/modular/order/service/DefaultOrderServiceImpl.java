package com.stylefeng.guns.order.modular.order.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.service.CinemaService;
import com.stylefeng.guns.api.cinema.vo.FieldPriceVo;
import com.stylefeng.guns.api.cinema.vo.FilmInfoVo;
import com.stylefeng.guns.api.order.service.OrderService;
import com.stylefeng.guns.api.order.vo.OrderStatusEnum;
import com.stylefeng.guns.api.order.vo.OrderVo;
import com.stylefeng.guns.order.modular.order.dao.CcOrderMapper;
import com.stylefeng.guns.order.modular.order.model.CcOrder;
import com.stylefeng.guns.order.util.FTPUtil;
import com.stylefeng.guns.order.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 指定服务的分组和版本
 */
@DubboService(group = "default", version = "1.0")
@Slf4j
public class DefaultOrderServiceImpl implements OrderService {

    @Autowired
    private FTPUtil ftpUtil;
    @Autowired
    private CcOrderMapper ccOrderMapper;

    @DubboReference(check = false)
    private CinemaService cinemaService;

    /**
     * 验证选购的作为是否存在
     *
     * @param fieldId 场次id
     * @param seats   选购的座位
     * @return
     */
    @Override
    public boolean isTrueSeats(String fieldId, String seats) {
        // 根据FieldId找到对应的座位信息存在ftp服务器什么位置
        String seatPath = ccOrderMapper.getSeatsByFieldId(fieldId);

        // 从ftp路径获取作为信息，得到座位id数组
        String seatsJson = ftpUtil.getFileStrByAddress(seatPath);
        JSONObject jsonObject = JSONObject.parseObject(seatsJson);
        JSONArray ids = (JSONArray) jsonObject.get("ids");

        // 判断选购座位是否存在
        List<Integer> collect = Arrays.stream(seats.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        return ids.containsAll(collect);
    }

    /**
     * 判断是否为已售座位
     *
     * @param fieldId 场次id
     * @param seats   选购的座位
     * @return
     */
    @Override
    public boolean isNotSoldSeats(String fieldId, String seats) {
        EntityWrapper<CcOrder> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("field_id", fieldId);
        List<CcOrder> orderList = ccOrderMapper.selectList(entityWrapper);

        String[] seatArrs = seats.split(",");
        // 有任何一个编号匹配上，则直接返回失败
        for (CcOrder ccOrder : orderList) {
            String[] ids = ccOrder.getSeatsIds().split(",");
            for (String id : ids) {
                for (String seat : seatArrs) {
                    if (id.equalsIgnoreCase(seat)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 创建新的订单
     *
     * @param fieldId   场次id
     * @param soldSeats 选购座位
     * @param seatsName 座位名称
     * @param userId    用户id
     * @return
     */
    @Override
    public OrderVo saveOrderInfo(Integer fieldId, String soldSeats, String seatsName, Integer userId) {
        // 编号
        String uuid = UUIDUtil.genUuid();
        // 影片信息
        FilmInfoVo filmInfoVo = cinemaService.getFilmInfoByFieldId(fieldId);
        Integer filmId = Integer.parseInt(filmInfoVo.getFilmId());
        // 获取影院信息
        FieldPriceVo fieldPriceVo = cinemaService.getPriceByFieldId(fieldId);
        Integer cinemaId = Integer.parseInt(fieldPriceVo.getCinemaId());
        double filmPrice = Double.parseDouble(fieldPriceVo.getFilmPrice());

        // 求订单总金额  // 1,2,3,4,5
        int soldCount = soldSeats.split(",").length;
        double totalPrice = getTotalPrice(soldCount, filmPrice);

        CcOrder order = new CcOrder();
        order.setUuid(uuid);
        order.setSeatsName(seatsName);
        order.setSeatsIds(soldSeats);
        order.setOrderUser(userId);
        order.setOrderPrice(totalPrice);
        order.setFilmPrice(filmPrice);
        order.setFilmId(filmId);
        order.setFieldId(fieldId);
        order.setCinemaId(cinemaId);

        Integer insert = ccOrderMapper.insert(order);
        if (insert > 0) {
            // 返回查询结果
            OrderVo orderVo = ccOrderMapper.getOrderInfoById(uuid);
            if (orderVo == null || orderVo.getOrderId() == null) {
                log.error("订单信息查询失败,订单编号为{}", uuid);
                return null;
            } else {
                return orderVo;
            }
        } else {
            // 插入出错
            log.error("订单插入失败");
            return null;
        }
    }

    /**
     * 计算总价
     *
     * @param soldCount 购买数量
     * @param filmPrice 单价
     * @return
     */
    private static double getTotalPrice(int soldCount, double filmPrice) {
        BigDecimal filmPriceDeci = new BigDecimal(filmPrice);
        BigDecimal soldCountDeci = new BigDecimal(soldCount);

        BigDecimal result = filmPriceDeci.multiply(soldCountDeci);
        // 四舍五入，取小数点后两位
        BigDecimal bigDecimal = result.setScale(2, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    /**
     * 查询当前登陆人已经购买的订单
     *
     * @param userId
     * @param page
     * @return
     */
    @Override
    public List<OrderVo> getOrderByUserId(Integer userId, Page<OrderVo> page) {
        log.info("调用getOrderByUserId");
        if (userId == null) {
            log.error("订单查询业务失败，用户编号未传入");
            return null;
        }

        Page<OrderVo> result = new Page<>();
        List<OrderVo> ordersByUserId = ccOrderMapper.getOrdersByUserId(userId, page);
        if (ordersByUserId == null || ordersByUserId.size() == 0) {
            result.setTotal(0);
            result.setRecords(new ArrayList<>());
        } else {
            // 获取订单总数
            EntityWrapper<CcOrder> entityWrapper = new EntityWrapper<>();
            entityWrapper.eq("order_user", userId);
            Integer counts = ccOrderMapper.selectCount(entityWrapper);
            // 将结果放入Page
            result.setTotal(counts);
            result.setRecords(ordersByUserId);

        }
        return result.getRecords();
    }

    /**
     * 根据放映场次，获取已售出座位
     *
     * @param fieldId 场次id
     * @return
     */
    @Override
    public String getSoldSeatsByFieldId(Integer fieldId) {
        if (fieldId == null) {
            return "";
        }
        return ccOrderMapper.getSoldSeatsByFieldId(fieldId);
    }
    
    /**
     * 获取订单详情
     * @param orderId
     * @return
     */
    @Override
    public OrderVo getOrderInfoById(String orderId) {
        return ccOrderMapper.getOrderInfoById(orderId);
    }
    
    /**
     * 更新订单支付状态
     * @param orderId
     * @param status
     * @return
     */
    @Override
    public boolean updateOrderStatus(String orderId, OrderStatusEnum status) {
        // 一次调用，在多个dubbo服务互相调用时，都可以获得隐式参数
        Integer userId = (Integer) RpcContext.getContext().getObjectAttachment("userId");
        log.info("alipay服务获得隐式参数：userId={}", userId);
        if (userId == null || userId == 0) {
            return false;
        }
        
        CcOrder ccOrder = new CcOrder();
        ccOrder.setUuid(orderId);
        ccOrder.setOrderStatus(status.code());
        Integer integer = ccOrderMapper.updateById(ccOrder);
        return integer >= 1;
    }
}
