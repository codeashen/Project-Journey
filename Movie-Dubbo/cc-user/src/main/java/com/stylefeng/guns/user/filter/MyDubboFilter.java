package com.stylefeng.guns.user.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.*;

/**
 * Dubbo拦截器
 * 配置参考官方文档 http://dubbo.apache.org/zh-cn/blog/first-dubbo-filter/#dubbo-filter_3
 * 
 *  1. 创建拦截器类，实现dubbo的Filter接口（即本类）
 *  2. 在resource下创建目录 META-INF/dubbo，在其中创建文本文件 com.alibaba.dubbo.rpc.Filter
 *     写入 xxx=拦截器全限定类名
 *  3. 在@DubboService注解上设置拦截器，如@DubboService(filter={"xxx"})
 *  
 *  拦截器可以配置不同的粒度，DubboService、DubboReference都可以配置，具体参考官方文档
 */
@Slf4j
public class MyDubboFilter implements Filter {

    /**
     * Dubbo拦截器方法
     * @param invoker    被调用方法执行器
     * @param invocation 封装了被调用方法的信息
     * @return
     * @throws RpcException
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        log.info("方法[{}]拦截前, 参数:{}", invocation.getMethodName(), invocation.getArguments());
        Result result = invoker.invoke(invocation);
        log.info("方法[{}]拦截后, 返回值:{}", invocation.getMethodName(), result.getValue());
        return result;
    }
}
