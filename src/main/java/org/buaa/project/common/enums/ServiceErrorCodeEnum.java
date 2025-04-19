package org.buaa.project.common.enums;

import org.buaa.project.common.convention.errorcode.IErrorCode;

/**
 * 系统错误码
 */
public enum ServiceErrorCodeEnum implements IErrorCode {

    MAIL_SEND_ERROR("B000101", "邮件发送错误"),

    IMAGE_UPLOAD_ERROR("B000102", "图片上传错误"),

    FLOW_LIMIT_ERROR("B000103", "当前系统繁忙，请稍后再试");

    private final String code;

    private final String message;

    ServiceErrorCodeEnum(String code, String message) {
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
