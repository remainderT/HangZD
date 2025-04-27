package org.buaa.project.common.enums;

import org.buaa.project.common.convention.errorcode.IErrorCode;

public enum AnswerErrorCodeEnum implements IErrorCode {
    ANSWER_NULL("E000101", "该回答不存在"),
    ANSWER_POST_FAIL("E000102", "回答发布失败"),
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
