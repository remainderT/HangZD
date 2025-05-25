package org.buaa.project.dto.req.conversation;

import lombok.Data;

/**
 * 创建会话请求参数
 */
@Data
public class ConversationCreateReqDTO {

    /**
     * 用户2ID
     */
    private Long user2;

    /**
     * 所属问题ID
     */
    private Long questionId;
} 