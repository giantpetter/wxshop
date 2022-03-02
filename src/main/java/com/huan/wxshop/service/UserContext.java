package com.huan.wxshop.service;

import com.huan.wxshop.generate.User;

/**
 * 用于查看登录状态。
 * 每个线程独有的变量---用户
 */
public class UserContext {
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    public static void clear() {
        currentUser.remove();
    }


}
