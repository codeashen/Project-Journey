package com.ashen.ccfilm.hystrix;

import com.google.common.collect.Lists;
import com.netflix.hystrix.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 请求合并处理对象
 */
public class CommandCollapserDemo extends HystrixCollapser<List<String>, String, Integer> {

    private Integer id;

    public CommandCollapserDemo(Integer id) {
        super(Setter
                .withCollapserKey(HystrixCollapserKey.Factory.asKey("CommandCollapser"))
                .andCollapserPropertiesDefaults(
                        HystrixCollapserProperties.defaultSetter()
                                .withTimerDelayInMilliseconds(1000)
                )
        );
        this.id = id;
    }

    /**
     * 获取请求参数
     * @return
     */
    @Override
    public Integer getRequestArgument() {
        return id;
    }

    /**
     * 批量业务处理
     * @param requests
     * @return
     */
    @Override
    protected HystrixCommand<List<String>> createCommand(Collection<CollapsedRequest<String, Integer>> requests) {
        return new BatchCommand(requests);
    }

    /**
     * 批量处理结果 与 请求业务 之间映射关系处理
     * @param batchResponse 批量处理结果
     * @param requests 请求对象集合，包含每一个请求参数
     */
    @Override
    protected void mapResponseToRequests(List<String> batchResponse, Collection<CollapsedRequest<String, Integer>> requests) {
        int counts = 0;
        Iterator<HystrixCollapser.CollapsedRequest<String, Integer>> iterator = requests.iterator();
        while (iterator.hasNext()) {
            HystrixCollapser.CollapsedRequest<String, Integer> response = iterator.next();

            String result = batchResponse.get(counts++);

            response.setResponse(result);
        }
    }
}


class BatchCommand extends HystrixCommand<List<String>> {

    private Collection<HystrixCollapser.CollapsedRequest<String, Integer>> collection;

    public BatchCommand(Collection<HystrixCollapser.CollapsedRequest<String, Integer>> collection) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("BatchCommand")));
        this.collection = collection;
    }

    @Override
    protected List<String> run() throws Exception {
        System.err.println("currentThread : " + Thread.currentThread().getName());
        List<String> result = Lists.newArrayList();

        Iterator<HystrixCollapser.CollapsedRequest<String, Integer>> iterator = collection.iterator();
        while (iterator.hasNext()) {
            HystrixCollapser.CollapsedRequest<String, Integer> request = iterator.next();
            Integer reqParam = request.getArgument();

            // 具体业务逻辑
            result.add("req: " + reqParam);
        }

        return result;
    }
}
