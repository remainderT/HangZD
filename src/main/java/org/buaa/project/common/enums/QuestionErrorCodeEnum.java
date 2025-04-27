package org.buaa.project.common.enums;

import org.buaa.project.common.convention.errorcode.IErrorCode;

/**
 * 系统错误码
 */
public enum QuestionErrorCodeEnum implements IErrorCode {

    QUESTION_NULL("C000101", "该提问不存在"),
    QUESTION_USER_INCORRECT("C000102","该提问并非该用户所有"),
    QUESTION_POST_FAIL("C000201", "提问发布失败");

    private final String code;

    private final String message;

    QuestionErrorCodeEnum(String code, String message) {
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
