package org.buaa.project.dto.req.question;

import lombok.Data;

/**
 * 标记问题已解决请求参数
 */
@Data
public class QuestionSolveReqDTO {

    /**
     * 问题id
     */
    private Long id;

    /**
     * 是否满意
     */

    private Boolean satisfied;
}
