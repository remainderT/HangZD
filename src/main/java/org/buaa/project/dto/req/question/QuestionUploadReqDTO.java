package org.buaa.project.dto.req.question;

import lombok.Data;

/**
 * 问题上传请求参数
 */
@Data
public class QuestionUploadReqDTO {

    /**
     * 包含的图片
     */
    private String images;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

}
