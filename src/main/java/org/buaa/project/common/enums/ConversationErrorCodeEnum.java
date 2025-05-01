package org.buaa.project.common.enums;

import org.buaa.project.common.convention.errorcode.IErrorCode;

/**
 * 会话错误码枚举
 */
public enum ConversationErrorCodeEnum implements IErrorCode {

    CONVERSATION_NOT_FOUND("B000301", "会话不存在"),
    CONVERSATION_ALREADY_EXISTS("B000302", "会话已存在"),
    CONVERSATION_ACCESS_DENIED("B000303", "无权访问该会话");

    private final String code;

    private final String message;

    ConversationErrorCodeEnum(String code, String message) {
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