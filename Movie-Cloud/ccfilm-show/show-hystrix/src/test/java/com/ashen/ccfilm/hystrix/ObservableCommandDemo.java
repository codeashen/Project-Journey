package com.ashen.ccfilm.hystrix;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * HystrixObservableCommand 子类
 */
public class ObservableCommandDemo extends HystrixObservableCommand<String> {

    private final String commandKey;

    public ObservableCommandDemo(String commandKey) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("ObserveCommandDemo"))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey)));
        this.commandKey = commandKey;
    }

    @Override
    protected Observable<String> construct() {
        System.err.println("current Thread: " + Thread.currentThread().getName());

        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                // 业务处理
                subscriber.onNext("action 1, commandKey=" + commandKey);
                subscriber.onNext("action 2, commandKey=" + commandKey);
                subscriber.onNext("action 3, commandKey=" + commandKey);

                // 业务处理结束
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }
}
