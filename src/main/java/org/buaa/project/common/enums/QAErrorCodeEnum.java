package org.buaa.project.common.enums;

import org.buaa.project.common.convention.errorcode.IErrorCode;

/**
 * 问答错误码
 */
public enum QAErrorCodeEnum implements IErrorCode {

    ANSWER_NULL("C000101", "该回答不存在"),

    ANSWER_USER_INCORRECT("C000102", "该回答并非该用户所有"),

    ANSWER_POST_FAIL("C000103", "回答发布失败"),

    QUESTION_NULL("C000201", "该问题不存在"),

    QUESTION_USER_INCORRECT("C000202","该问题并非该用户所有"),

    QUESTION_POST_FAIL("C000203", "问题发布失败");

    private final String code;

    private final String message;

    QAErrorCodeEnum(String code, String message) {
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
