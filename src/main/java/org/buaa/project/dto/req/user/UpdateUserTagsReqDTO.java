package org.buaa.project.dto.req.user;

import lombok.Data;

/**
 * 更新用户标签请求参数
 */
@Data
public class UpdateUserTagsReqDTO {

    /**
     * 标签
     */
    private String tags;
} 