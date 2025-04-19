package org.buaa.project.common.convention.errorcode;

/**
 * 基础错误码定义
 */
public enum BaseErrorCode implements IErrorCode {

    // ========== 一级宏观错误码 客户端错误 ==========
    CLIENT_ERROR("A000001", "用户端错误"),

    // ========== 二级宏观错误码 用户注册错误 ==========
    USER_REGISTER_ERROR("A000100", "用户注册错误"),

    // ========== 二级宏观错误码 用户登录错误 ==========
    USER_LOGIN_ERROR("A000200", "用户登录错误"),

    // ========== 二级宏观错误码 用户系统请求错误 ==========
    FLOW_LIMIT_ERROR("A000300", "用户系统请求错误"),

    // ========== 一级宏观错误码 系统执行出错 ==========
    SERVICE_ERROR("B000001", "系统执行出错"),
    // ========== 二级宏观错误码 系统执行超时 ==========
    SERVICE_TIMEOUT_ERROR("B000100", "系统执行超时"),

    // ========== 一级宏观错误码 调用第三方服务出错 ==========
    REMOTE_ERROR("C000001", "调用第三方服务出错");

    private final String code;

    private final String message;

    BaseErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
