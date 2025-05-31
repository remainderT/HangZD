package org.buaa.project.dto.resp;

import lombok.Data;

import java.util.Date;

/**
 * 公开会话分页查询响应
 */
@Data
public class ConversationPageRespDTO {

    /**
     * id
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
     * 日期
     */
    private Date createTime;

}