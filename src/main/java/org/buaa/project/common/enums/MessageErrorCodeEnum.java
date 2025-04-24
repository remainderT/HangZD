package org.buaa.project.common.enums;

import org.buaa.project.common.convention.errorcode.IErrorCode;

public enum MessageErrorCodeEnum implements IErrorCode {
    MESSAGE_NOT_FOUND("D000101", "指定id的消息不存在"),
    MESSAGE_SENDER_INCORRECT("D000102", "消息发送者不正确"),
    MESSAGE_EMPTY_MESSAGE("D000201", "消息内容不能为空"),
;

    private final String code;

    private final String message;

    MessageErrorCodeEnum(String code, String message) {
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
