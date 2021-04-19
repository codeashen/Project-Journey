package com.ashen.ccfilm.cinema.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ashen.ccfilm.cinema.entity.vo.CinemaSavedReqVo;
import com.ashen.ccfilm.cinema.entity.vo.DescribeCinemasRespVO;
import com.ashen.ccfilm.cinema.service.CinemaService;
import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.vo.BasePageVo;
import com.ashen.ccfilm.common.vo.DataResult;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 影院模块表现层
 */
@Slf4j
@RestController
@RequestMapping("/cinema")
public class CinemaController {

    @Autowired
    private CinemaService cinemaService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public DataResult<String> saveCinema(@RequestBody CinemaSavedReqVo cinemaSavedReqVo) throws CommonServiceException {
        // 数据验证
        cinemaSavedReqVo.checkParam();
        cinemaService.saveCinema(cinemaSavedReqVo);
        return DataResult.success();
    }

    /**
     * 配置了Hystrix的业务方法
     * @see CinemaController#describeCinemasFallback(BasePageVo)
     */
    // 使用 HystrixCommand 执行方法
    @HystrixCommand(
            // fallback方法
            fallbackMethod = "describeCinemasFallback",
            // command属性配置
            commandProperties = {    
                    @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"),                  // 线程隔离
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value= "1000"), // 超时时间
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),             // 快照时间窗内请求总数阈值
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")            // 快照时间窗内错误百分比阈值
            },
            // 线程池属性配置
            threadPoolProperties = {  
                    @HystrixProperty(name = "coreSize", value = "1"),  // 核心线程数
                    @HystrixProperty(name = "maxQueueSize", value = "10"),  // 等待队列大小
                    @HystrixProperty(name = "keepAliveTimeMinutes", value = "1000"),  // 线程存活时间
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "8"),  // 任务队列的请求上限
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),  // 生成统计数据流时滑动窗口应该拆分的桶数
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1500")  // 此配置项指定了窗口的大小
            },
            // 忽略的异常, 抛出此异常不触发fallback
            ignoreExceptions = CommonServiceException.class
    )
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public DataResult<String> describeCinemas(BasePageVo basePageVo) throws CommonServiceException {

        IPage<DescribeCinemasRespVO> describeCinemasRespVOIPage = cinemaService.describeCinemas(basePageVo.getNowPage(), basePageVo.getPageSize());

        if (basePageVo.getNowPage() > 10000) {
            // 此异常不触发fallback, 配置了异常忽略
            // throw new CommonServiceException(400, "nowPage太大了，不支持此处理");
            
            // 超时触发 fallback
            try {
                TimeUnit.SECONDS.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // TODO 调用封装的分页返回方法
        return DataResult.success();
    }

    /**
     * fallback方法
     * 参数和返回值必须要和指定的真正的业务方法相同
     * @see CinemaController#describeCinemas(BasePageVo)
     */
    public DataResult<String> describeCinemasFallback(BasePageVo basePageVo) throws CommonServiceException {
        log.error("出错了, 进入fallback方法");
        return DataResult.success("请求降级处理返回");
    }

}
