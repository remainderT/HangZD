package org.buaa.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 回答查询响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRespDTO {

    /**
     * 回答id
     */
    private Long id;

    /**
     * 发布人id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

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

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 是否有用
     */
    private Integer useful;

    /**
     * 更新时间
     */
    private Date updateTime;
}
