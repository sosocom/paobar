package com.sosocom.common;

/**
 * 当前请求用户上下文（ThreadLocal），由 JwtAuthFilter 设置。
 * 默认仅在需要管理员鉴权时填充 IS_ADMIN，非管理员路径不强制查询。
 */
public class RequestContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_ADMIN = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setIsAdmin(Boolean admin) {
        IS_ADMIN.set(admin);
    }

    /** 当前请求用户是否管理员，未填充时返回 false。 */
    public static boolean isAdmin() {
        Boolean v = IS_ADMIN.get();
        return v != null && v;
    }

    public static void clear() {
        USER_ID.remove();
        IS_ADMIN.remove();
    }
}
