package com.ashen.ccfilm.hystrix;

import com.netflix.hystrix.*;

/**
 * HystrixCommand 子类
 */
public class CommandDemo extends HystrixCommand<String> {

    private final String commandKey;

    public CommandDemo(String commandKey) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("CommandHelloWorld"))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.defaultSetter()
                                .withRequestCacheEnabled(false)     //请求缓存开关
                                .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE) //信号量隔离
                                .withExecutionIsolationSemaphoreMaxConcurrentRequests(2)    //任务执行信号量最大数
                                .withFallbackIsolationSemaphoreMaxConcurrentRequests(3)     //失败任务执行信号量最大数
                                // .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD) //线程池隔离

                                // .withCircuitBreakerForceOpen(true)           // 强制开启熔断器
                                .withCircuitBreakerRequestVolumeThreshold(2)    // 单位时间内的请求阈值
                                .withCircuitBreakerErrorThresholdPercentage(50) // 当满足请求阈值时，超过50%则开启熔断
                )
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("MyThreadPool"))  //线程池名称
                .andThreadPoolPropertiesDefaults(       //线程池配置
                        HystrixThreadPoolProperties.defaultSetter()
                                .withCoreSize(2)        //核心线程数
                                .withMaximumSize(3).withAllowMaximumSizeToDivergeFromCoreSize(true)  //最大线程数、开启最大线程
                                .withMaxQueueSize(2)    //等待队列大小
                )
        );
        this.commandKey = commandKey;
    }

    /**
     * 业务执行方法
     * 单次请求调用的业务方法
     */
    @Override
    protected String run() throws Exception {
        String result = "Command run method, commandKey: " + commandKey;

        // Thread.sleep(800L);

        if (commandKey.startsWith("exception")) {
            int i = 6 / 0;
        }

        System.err.println(result + " , currentThread-" + Thread.currentThread().getName());
        // System.err.println(result + " , currentThread-" + Thread.currentThread().getName());
        return result;
    }

    /**
     * 降级方法
     * run方法出现异常会执行fallback方法
     */
    @Override
    protected String getFallback() {
        String result = "Command fallback method, commandKey: " + commandKey;
        System.err.println(result + " , currentThread-" + Thread.currentThread().getName());
        return result;
    }

    /**
     * 设置本地缓存key
     */
    @Override
    protected String getCacheKey() {
        return String.valueOf(commandKey);
    }
}

