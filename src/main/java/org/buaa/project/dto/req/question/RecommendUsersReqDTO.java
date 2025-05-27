package org.buaa.project.dto.req.question;

import lombok.Data;

/**
 * 推荐回到用户参数
 */
@Data
public class RecommendUsersReqDTO {
    
    /**
     * 问题内容
     */
    private String question;
}
