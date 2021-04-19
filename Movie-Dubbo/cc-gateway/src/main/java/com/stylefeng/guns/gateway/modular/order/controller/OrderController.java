package com.stylefeng.guns.gateway.modular.order.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.stylefeng.guns.api.alipay.AliPayService;
import com.stylefeng.guns.api.alipay.vo.AliPayInfoVo;
import com.stylefeng.guns.api.alipay.vo.AliPayResultVo;
import com.stylefeng.guns.api.order.service.OrderService;
import com.stylefeng.guns.api.order.vo.OrderVo;
import com.stylefeng.guns.core.util.TokenBucket;
import com.stylefeng.guns.core.util.ToolUtil;
import com.stylefeng.guns.gateway.common.CurrentUserHolder;
import com.stylefeng.guns.gateway.common.vo.DataResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/order")
public class OrderController {

    // 自定义令牌桶（实验用）
    private static final TokenBucket tokenBucket = new TokenBucket(100);
    // 文件服务器地址
    private static final String IMG_PRE = "http://img.meetingshop.cn/";

    /**
     * group   指定调用的是哪个分组的服务，用来区分不同的服务实现，可以逗号分割多个，也可以通配符
     * version 配置调用的服务的版本
     * merger  配置是否合并结果，当返回的是List，会将调用的所有服务结果合并起来
     * 如果存在多相同分组或相同版本的服务提供者，只会调用其中一个一次，不会都走一遍
     * 
     * mock    设置本地存根，如果服务提供方访问不到或超时，问访问本地伪装方法
     *         不启动AliPayService服务，或者在其中设置超时，就可以访问到本地伪装
     */
    @DubboReference(check = false, group = "default", version = "1.0")
    private OrderService orderService;
    @DubboReference(check = false, group = "*", merger = "true")
    private OrderService orderServiceAll;
    @DubboReference(check = false, mock = "com.stylefeng.guns.api.alipay.AliPayServiceMock")
    private AliPayService aliPayService;

    /**
     * buyTickets方法的失败回调方法，方法的返回值和参数必须和buyTickets方法一样
     */
    public DataResult<OrderVo> buyTicketsError(Integer fieldId, String soldSeats, String seatsName) {
        return DataResult.serviceFail("系统繁忙中，请稍后重试！");
    }

    /**
     * 买票
     * 配置Hystrix熔断器，配置解析可参考 https://www.jianshu.com/p/14958039fd15
     *
     * @param fieldId
     * @param soldSeats
     * @param seatsName
     * @return
     */
    @PostMapping("buyTickets")
    @HystrixCommand(fallbackMethod = "buyTicketsError", // 指定失败回调方法为buyTicketsError方法
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"),          //策略：thread
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "4000"),  // 超时时间4秒
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),     // 一段时间内(10s)内10个请求出错则打开熔断器
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")},  // 一段时间内(10s)内请求个数的50%个出错则打开熔断器
            threadPoolProperties = {      // 线程池配置
                    @HystrixProperty(name = "coreSize", value = "1"),
                    @HystrixProperty(name = "maxQueueSize", value = "10"),
                    @HystrixProperty(name = "keepAliveTimeMinutes", value = "1000"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "8"),
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1500")})
    public DataResult<OrderVo> buyTickets(Integer fieldId, String soldSeats, String seatsName) {
        // 自定义令牌桶校验
        if (!tokenBucket.getToken()) {
            return DataResult.serviceFail("购票人数过多，请稍后重试");
        }

        // 创建订单信息,注意获取登陆人
        Integer userId = CurrentUserHolder.getCurrentUser();
        if (userId == null || userId == 0) {
            return DataResult.serviceFail("用户未登陆");
        }
        // 验证售出的票是否为真
        boolean isTrue = orderService.isTrueSeats(fieldId + "", soldSeats);
        // 已经销售的座位里，有没有这些座位
        boolean isNotSold = orderService.isNotSoldSeats(fieldId + "", soldSeats);
        if (!isTrue || !isNotSold) {
            return DataResult.serviceFail("订单中的座位编号有问题");
        }

        OrderVo orderVo = orderService.saveOrderInfo(fieldId, soldSeats, seatsName, userId);
        if (orderVo == null) {
            log.error("购票未成功");
            return DataResult.serviceFail("购票业务异常");
        } else {
            return DataResult.success(orderVo);
        }
    }

    /**
     * 查询订单信息
     *
     * @param nowPage
     * @param pageSize
     * @return
     */
    @GetMapping("getOrderInfo")
    public DataResult<List<OrderVo>> getOrderInfo(
            @RequestParam(name = "nowPage", required = false, defaultValue = "1") Integer nowPage,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5") Integer pageSize) {
        // 获取当前登陆人的信息
        Integer userId = CurrentUserHolder.getCurrentUser();
        if (userId == null || userId == 0) {
            return DataResult.serviceFail("用户未登陆");
        }

        // 使用当前登陆人获取已经购买的订单
        Page<OrderVo> page = new Page<>(nowPage, pageSize);
        List<OrderVo> result = orderServiceAll.getOrderByUserId(userId, page);

        return DataResult.success(nowPage, result.size(), "", result);
    }

    /**
     * 获取支付二维码
     *
     * @param orderId
     * @return
     */
    @RequestMapping(value = "getPayInfo", method = RequestMethod.POST)
    public DataResult<AliPayInfoVo> getPayInfo(@RequestParam("orderId") String orderId) {
        // 获取当前登陆人的信息
        Integer userId = CurrentUserHolder.getCurrentUser();
        if (userId == null || userId == 0) {
            return DataResult.serviceFail("抱歉，用户未登陆");
        }
        // 订单二维码返回结果
        AliPayInfoVo aliPayInfoVo = aliPayService.getQRCode(orderId);
        return DataResult.success(IMG_PRE, aliPayInfoVo);
    }

    /**
     * 获取支付状态
     *
     * @param orderId
     * @param tryNums
     * @return
     */
    @RequestMapping(value = "getPayResult", method = RequestMethod.POST)
    public DataResult getPayResult(
            @RequestParam("orderId") String orderId,
            @RequestParam(name = "tryNums", required = false, defaultValue = "1") Integer tryNums) {
        // 获取当前登陆人的信息
        Integer userId = CurrentUserHolder.getCurrentUser();
        if (userId == null || userId == 0) {
            return DataResult.serviceFail("抱歉，用户未登陆");
        }
        
        // 使用Dubbo的隐式参数，将用户信息传递到服务端
        RpcContext.getContext().setAttachment("userId", userId);

        // 判断是否支付超时
        if (tryNums >= 4) {
            return DataResult.serviceFail("订单支付失败，请稍后重试");
        } else {
            AliPayResultVo aliPayResultVo = aliPayService.getOrderStatus(orderId);
            if (aliPayResultVo == null || ToolUtil.isEmpty(aliPayResultVo.getOrderId())) {
                aliPayResultVo = new AliPayResultVo();
                aliPayResultVo.setOrderId(orderId);
                aliPayResultVo.setOrderStatus(0);
                aliPayResultVo.setOrderMsg("支付不成功");
            }
            return DataResult.success(aliPayResultVo);
        }
    }
}
