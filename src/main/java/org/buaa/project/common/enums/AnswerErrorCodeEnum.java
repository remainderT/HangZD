package org.buaa.project.common.enums;

import org.buaa.project.common.convention.errorcode.IErrorCode;

public enum AnswerErrorCodeEnum implements IErrorCode {
    MESSAGE_NOT_FOUND("E000101", "该回答不存在")
    ;

    private final String code;

    private final String message;

    AnswerErrorCodeEnum(String code, String message) {
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
