package org.buaa.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回消息
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
     * 创建时间
     */
    private String createTime;
    /**
     * 更新时间
     */
    private String updateTime;
    /**
     * 是否被删除
     */
    private Integer delFlag;
    /**
     * 是否已回答
     */
    private Integer answered;
}
