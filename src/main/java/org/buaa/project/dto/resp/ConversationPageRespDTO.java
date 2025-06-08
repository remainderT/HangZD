package org.buaa.project.dto.resp;

import lombok.Data;

import java.util.Date;

/**
 * 公开会话分页查询响应
 */
@Data
public class ConversationPageRespDTO {

    /**
     * 会话id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 点赞状态
     */
    private String likeStatus;

    /**
     * 点赞数量
     */
    private Integer likeCount;

    /**
     * 日期
     */
    private Date createTime;

}