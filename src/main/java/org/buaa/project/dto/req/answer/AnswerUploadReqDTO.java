package org.buaa.project.dto.req.answer;

import lombok.Data;

/**
 * 标记回答更新请求参数
 */
@Data
public class AnswerUploadReqDTO {
    /**
     * 发布人id
     */
    private Long user_id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 对应问题的id
     */
    private Long question_id;
    /**
     * 内容
     */
    private String content;
    /**
     * 包含的图片
     */
    private String images;


}
