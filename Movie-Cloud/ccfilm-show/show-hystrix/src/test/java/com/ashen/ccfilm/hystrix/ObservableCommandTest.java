package com.ashen.ccfilm.hystrix;

import org.junit.jupiter.api.Test;
import rx.Observable;
import rx.Subscriber;

public class ObservableCommandTest {

    @Test
    public void observeTest() throws InterruptedException {
        long beginTime = System.currentTimeMillis();

        ObservableCommandDemo commandDemo = new ObservableCommandDemo("ObservableCommandTest-observe");

        Observable<String> observe = commandDemo.observe();

        // 阻塞式调用
        // String result = observe.toBlocking().single();
        //
        // long endTime = System.currentTimeMillis();
        // System.out.println("result = " + result + " , speeding=" + (endTime - beginTime));

        // 非阻塞式调用
        observe.subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                System.err.println("ObserveCommandTest-observe , onCompleted");
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("ObserveCommandTest-observe , onError - throwable=" + throwable);
            }

            @Override
            public void onNext(String result) {
                long endTime = System.currentTimeMillis();
                System.err.println(
                        "ObserveCommandTest-observe , onNext result = " + result + " speend:" + (endTime - beginTime));
            }
        });

        Thread.sleep(1000L);
    }
}
