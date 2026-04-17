package com.sosocom.common;

/**
 * 当前请求用户上下文（ThreadLocal），由 JwtAuthFilter 设置
 */
public class RequestContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        USER_ID.remove();
    }
}
