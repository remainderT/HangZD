package org.buaa.project.common.enums;

/**
 * 用户行为类型枚举
 */
public enum UserActionTypeEnum {

    COLLECT("collect"),

    ANSWER( "answer"),

    LIKE( "like"),

    RECOMMEND( "recommend");

    private final String type;

    UserActionTypeEnum(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }
}