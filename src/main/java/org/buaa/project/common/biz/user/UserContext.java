package org.buaa.project.common.biz.user;

import java.util.Optional;

/**
 * 用户上下文
 */
public final class UserContext {


    private static final ThreadLocal<UserInfoDTO> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置用户至上下文
     *
     * @param user 用户详情信息
     */
    public static void setUser(UserInfoDTO user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取上下文中用户 ID
     *
     * @return 用户 ID
     */
    public static Long getUserId() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUserId).map(Long::valueOf).orElse(null);
    }

    /**
     * 获取上下文中用户名称
     *
     * @return 用户名称
     */
    public static String getUsername() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUsername).orElse(null);
    }

    /**
     * 获取上下文中用户类别
     *
     * @return 用户类别
     */
    public static String getUserType() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUserType).orElse(null);
    }

    /**
     * 获取上下文中用户 Token
     *
     * @return 用户 Token
     */
    public static String getToken() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getToken).orElse(null);
    }

    /**
     * 获取上下文中用户盐
     *
     * @return 用户盐
     */
    public static String getSalt() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getSalt).orElse(null);
    }

    /**
     * 清理用户上下文
     */
    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }
}