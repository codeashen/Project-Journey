package com.ashen.ccfilm.hystrix;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class CommandTest {

    /**
     * execute 同步执行
     */
    @Test
    public void executeTest() {
        long beginTime = System.currentTimeMillis();

        CommandDemo commandDemo = new CommandDemo("execute");
        // CommandDemo commandDemo = new CommandDemo("exception");
        // 调用execute方法，执行一系列检查，最后执行run方法（业务方法）
        String result = commandDemo.execute();
        log.info(result);

        long endTime = System.currentTimeMillis();
        log.info("execute 耗时 : {} ms", endTime - beginTime);
    }

    /**
     * queue 异步执行
     */
    @Test
    public void queueTest() throws ExecutionException, InterruptedException {
        long beginTime = System.currentTimeMillis();

        CommandDemo commandDemo = new CommandDemo("queue");
        Future<String> future = commandDemo.queue();

        long endTime = System.currentTimeMillis();
        log.info("queue得到Future耗时 : {}ms", endTime - beginTime);

        String result = future.get();
        long endTime2 = System.currentTimeMillis();
        log.info("得到结果 ：【{}】 , 总耗时 : {}ms", result, endTime2 - beginTime);
    }

    /**
     * observe 执行
     */
    @Test
    public void observeTest() throws Exception {
        long beginTime = System.currentTimeMillis();

        CommandDemo commandDemo = new CommandDemo("observe");
        Observable<String> observe = commandDemo.observe();

        // 阻塞式调用
        String result = observe.toBlocking().single();

        long endTime = System.currentTimeMillis();
        log.info("observe阻塞式调用耗时 : {}ms, 结果 : {}", endTime - beginTime, result);

        // 非阻塞式调用
        observe.subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                log.warn("observe onCompleted");
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("observe 出错", throwable);
            }

            @Override
            public void onNext(String result) {
                long endTime2 = System.currentTimeMillis();
                log.warn("observe onNext, 耗时 : {}ms, 结果 : {}", endTime2 - beginTime, result);
            }
        });

        Thread.sleep(1000L);
    }

    /**
     * toObservable 执行
     * 和observe的区别就是得到的Observable对象不能复用
     */
    @Test
    public void toObservableTest() throws Exception {
        long beginTime = System.currentTimeMillis();

        CommandDemo commandDemo = new CommandDemo("toObservable");
        Observable<String> toObservable1 = commandDemo.toObservable();   // toObservable得到的Observable对象只能执行一次

        // 阻塞式调用
        String result = toObservable1.toBlocking().single();

        long endTime = System.currentTimeMillis();
        log.info("toObservable阻塞式调用耗时 : {}ms, 结果 : {}", endTime - beginTime, result);

        // 非阻塞式调用（和observe的区别就是得到的Observable对象不能复用）
        CommandDemo commandDemo2 = new CommandDemo("toObservable2");
        Observable<String> toObservable2 = commandDemo2.toObservable();   // 再次执行需要重新获取Observable对象
        toObservable2.subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                log.warn("toObservable onCompleted");
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("toObservable 出错", throwable);
            }

            @Override
            public void onNext(String result) {
                long endTime2 = System.currentTimeMillis();
                log.warn("toObservable onNext, 耗时 : {}ms, 结果 : {}", endTime2 - beginTime, result);
            }
        });

        Thread.sleep(2000L);
    }

    /**
     * 演示请求缓存
     */
    @Test
    public void requestCache() {
        // 开启请求上下文
        HystrixRequestContext requestContext = HystrixRequestContext.initializeContext();

        long beginTime = System.currentTimeMillis();
        CommandDemo c1 = new CommandDemo("c1");
        CommandDemo c2 = new CommandDemo("c2");
        CommandDemo c3 = new CommandDemo("c1");  // 会走本地缓存

        // 第一次请求
        String r1 = c1.execute();
        System.out.println("result=" + r1 + " , speeding=" + (System.currentTimeMillis() - beginTime));
        // 第二次请求
        String r2 = c2.execute();
        System.out.println("result=" + r2 + " , speeding=" + (System.currentTimeMillis() - beginTime));
        // 第三次请求
        String r3 = c3.execute();
        System.out.println("result=" + r3 + " , speeding=" + (System.currentTimeMillis() - beginTime));

        // 请求上下文关闭
        requestContext.close();
    }

    /**
     * 演示线程隔离内容
     */
    @Test
    public void threadTest() throws ExecutionException, InterruptedException {
        CommandDemo c1 = new CommandDemo("c1");
        CommandDemo c2 = new CommandDemo("c2");
        CommandDemo c3 = new CommandDemo("c3");
        CommandDemo c4 = new CommandDemo("c4");
        CommandDemo c5 = new CommandDemo("c5");

        Future<String> q1 = c1.queue();
        Future<String> q2 = c2.queue();
        Future<String> q3 = c3.queue();
        Future<String> q4 = c4.queue();
        Future<String> q5 = c5.queue();

        String r1 = q1.get();
        String r2 = q2.get();
        String r3 = q3.get();
        String r4 = q4.get();
        String r5 = q5.get();

        System.out.println(r1+","+r2+","+r3+","+r4+","+r5);

        // 1,2,3,4,5
        // core 1,2  max 1
        // queue 2
    }

    /**
     * 信号量隔离测试
     */
    @Test
    public void semTest() throws InterruptedException {
        MyThread t1 = new MyThread("t1");
        MyThread t2 = new MyThread("t2");
        MyThread t3 = new MyThread("t3");
        MyThread t4 = new MyThread("t4");
        MyThread t5 = new MyThread("t5");
        
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();

        Thread.sleep(2000L);
    }

    /**
     * 测试信号量隔离的自定义线程
     */
    class MyThread extends Thread {

        private String name;

        public MyThread(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            CommandDemo commandDemo = new CommandDemo(name);
            System.out.println("commandDemo result=" + commandDemo.execute());
        }
    }

    /**
     * 熔断演示
     */
    @Test
    public void cbTest() throws InterruptedException {
        // 正确 - 业务
        CommandDemo c1 = new CommandDemo("111");
        System.out.println(c1.execute());

        // 错误 - 业务
        CommandDemo c2 = new CommandDemo("exception-1");
        System.out.println(c2.execute());

        // 正确 - 业务
        Thread.sleep(1000L);
        CommandDemo c3 = new CommandDemo("222");
        System.out.println(c3.execute());

        // 半熔断状态
        Thread.sleep(5000L);
        
        // 错误 - 业务
        CommandDemo c4 = new CommandDemo("exception-2");
        System.out.println(c4.execute());

        // 正确 - 业务
        CommandDemo c5 = new CommandDemo("333");
        System.out.println(c5.execute());
        
        // 成功
        CommandDemo c6 = new CommandDemo("444");
        System.out.println(c6.execute());

    }

}