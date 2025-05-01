package org.buaa.project.common.enums;

import lombok.Getter;

/**
 * 实体类型枚举
 */
@Getter
public enum EntityTypeEnum {
    
    USER("user", "用户"),

    QUESTION("question", "问题"),

    ANSWER("answer", "回答");
    
    private final String code;

    private final String desc;
    
    EntityTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 