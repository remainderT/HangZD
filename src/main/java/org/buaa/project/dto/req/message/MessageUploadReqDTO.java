package org.buaa.project.dto.req.message;

import lombok.Data;

import java.sql.Date;

/**
 * 标记消息上传请求参数
 */
@Data
public class MessageUploadReqDTO {
    /**
     * 消息发送者id
     */

    private Long fromId;
    /**
     * 消息接收者id
     */
    private Long toId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private String type; // 与数据库中的 ENUM 类型对应
    /**
     * 创建时间
     */
    //private Date createTime;


}
