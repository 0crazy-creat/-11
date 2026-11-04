package com.csp.security.core;

import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.common.security.LoginUser;

/** 线程级登录用户上下文。 */
public final class UserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(LoginUser user) { HOLDER.set(user); }

    public static LoginUser get() { return HOLDER.get(); }

    public static LoginUser require() {
        LoginUser u = HOLDER.get();
        if (u == null) throw BizException.unauthorized("请先登录");
        return u;
    }

    public static Long requireUserId() { return require().getUserId(); }

    public static Long currentContestId() { return require().getContestId(); }

    public static boolean isAdmin() {
        LoginUser u = HOLDER.get();
        return u != null && (u.hasRole("ADMIN") || u.hasPermission("*"));
    }

    public static void checkPermission(String perm) {
        if (!require().hasPermission(perm)) throw new BizException(ResultCode.FORBIDDEN, "缺少权限：" + perm);
    }

    public static void checkRole(String role) {
        if (!require().hasRole(role)) throw BizException.forbidden("需要角色：" + role);
    }

    public static void clear() { HOLDER.remove(); }
}
