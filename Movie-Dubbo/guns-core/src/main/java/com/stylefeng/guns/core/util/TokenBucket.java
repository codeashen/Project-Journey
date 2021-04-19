package com.stylefeng.guns.core.util;

import lombok.Data;

/**
 * 自定义令牌桶算法限流器（简单模拟）
 * 
 * 创建令牌桶的时候传入初始令牌数量
 * 每毫秒会增加 INC_RATE 个令牌
 * 每次获取成功时，令牌数 size 减去 1
 */
@Data
public class TokenBucket {
    // 令牌桶的容量
    private final int CAPACITY = 100;
    // 令牌流入速度，每毫秒向桶内增加的令牌数量
    private final int INC_RATE = 1;
    // 当前令牌数量
    private int size;
    // 时间戳，获取令牌时间会更新
    private long timestamp;

    /**
     * 构造方法初始化令牌数量
     * @param size 初始容量
     */
    public TokenBucket(int size) {
        this.size = size <= this.CAPACITY && size >=0 ? size : 0;
    }

    /**
     * 尝试获取令牌方法
     * @return 布尔值，返回是否成功获取
     */
    public synchronized boolean getToken() {
        // 获取当前时间
        long currentTime = System.currentTimeMillis();
        // 计算当前桶中应该有多少令牌
        int increment = (int) (currentTime - timestamp) * INC_RATE;  // 令牌自然增长量
        increment = increment >= 0 ? increment : CAPACITY;  // 解决算数失真产生负值的情况
        size = Math.min((size + increment), CAPACITY);   // 当前最新令牌数量
        // 更新时间戳
        timestamp = currentTime;
        // 判断是否能够拿到令牌
        if (size > 0) {
            size--;
            return true;
        } else {
            return false;
        }
    }
    
}
