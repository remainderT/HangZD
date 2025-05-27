package org.buaa.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.buaa.project.common.database.BaseDO;

/**
 * 消息持久层实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("message")
public class MessageDO extends BaseDO {

    /**
     * ID
     */
    private Long id;

    /**
     * 发送人ID (1表示系统消息)
     */
    private Long fromId;

    /**
     * 接收者ID
     */
    private Long toId;

    /**
     * 会话id
     */
    private Long conversationId;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 生成消息的id
     */
    private Long generateId;

    /**
     * 消息状态 (0-未读; 1-已读)
     */
    private Integer status;
}
