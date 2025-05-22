package org.buaa.project.dto.req.answer;

import lombok.Data;

/**
 * 回答上传请求参数
 */
@Data
public class AnswerUploadReqDTO {

    /**
     * 对应问题的id
     */
    private Long questionId;

    /**
     * 内容
     */
    private String content;

    /**
     * 包含的图片
     */
    private String images;

}
