package com.stylefeng.guns.gateway.common;

/**
 * 使用ThreadLocal线程绑定的用户信息
 */
public class CurrentUserHolder {
    /**
     * 改用 ThreadLocal子类，可继承的 InheritableThreadLocal
     * 子线程会继承父线程ThreadLocal中存放的数据
     * 解决创建子线程后拿不到ThreadLocal中的值的情况
     */
    private static final InheritableThreadLocal<Integer> userIdHolder = new InheritableThreadLocal<>();

    public static void saveUserId(Integer userId) {
        userIdHolder.set(userId);
    }

    public static Integer getCurrentUser() {
        return userIdHolder.get();
    }
    
}
