package org.buaa.project.common.enums;

import lombok.Getter;

/**
 * 实体类型枚举
 */
@Getter
public enum EntityTypeEnum {

    SYSTEM("system"),

    QUESTION("question"),

    ANSWER("answer"),

    USER("user");

    private final String type;

    EntityTypeEnum(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }
} 