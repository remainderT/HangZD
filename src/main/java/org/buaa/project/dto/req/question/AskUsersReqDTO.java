package org.buaa.project.dto.req.question;

import lombok.Data;

import java.util.List;

/**
 * 问题推荐回答者请求参数
 */
@Data
public class AskUsersReqDTO {

    /**
     * 问题id
     */
    private Long questionId;

    /**
     * 用户id列表
     */
    private List<Long> userIds;
}
