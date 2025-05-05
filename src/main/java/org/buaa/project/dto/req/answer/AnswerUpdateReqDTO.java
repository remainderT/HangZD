package org.buaa.project.dto.req.answer;

import lombok.Data;

/**
 * 标记回答更新请求参数
 */
@Data
public class AnswerUpdateReqDTO {
    /**
     * 回答id
     */
    private Long id;

    /**
     * 回答内容
     */
    private String content;

    /**
     * 包含的图片
     */
    private String images;

}
