package org.buaa.project.dto.req.question;

import lombok.Data;

/**
 * 修改问题请求参数
 */
@Data
public class QuestionUpdateReqDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 包含的图片
     */
    private String images;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;
}
