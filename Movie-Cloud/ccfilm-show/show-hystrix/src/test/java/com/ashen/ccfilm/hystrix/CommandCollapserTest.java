package com.ashen.ccfilm.hystrix;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 请求合并测试
 */
public class CommandCollapserTest {

    @Test
    public void collapserTest() throws ExecutionException, InterruptedException {
        // 开启请求上下文
        HystrixRequestContext context = HystrixRequestContext.initializeContext();

        // 构建请求 -> 主要优化点，多个服务调用的多次HTTP请求合并，减少tcp建立链接握手次数
        // 缺点：很少有机会对同一个服务进行多次HTTP调用，同时还要足够的"近"
        CommandCollapserDemo c1 = new CommandCollapserDemo(1);
        CommandCollapserDemo c2 = new CommandCollapserDemo(2);
        CommandCollapserDemo c3 = new CommandCollapserDemo(3);
        CommandCollapserDemo c4 = new CommandCollapserDemo(4);

        // 获取结果, 足够的近 -> 10ms
        Future<String> q1 = c1.queue();
        Future<String> q2 = c2.queue();
        Future<String> q3 = c3.queue();
        Future<String> q4 = c4.queue();

        String r1 = q1.get();
        String r2 = q2.get();
        String r3 = q3.get();
        String r4 = q4.get();

        // 打印
        System.err.println(r1 + ", " + r2 + ", " + r3 + ", " + r4);

        // 关闭请求上下文
        context.close();
    }

}
