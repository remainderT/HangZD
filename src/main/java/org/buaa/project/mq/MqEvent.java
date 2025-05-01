package org.buaa.project.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.buaa.project.common.enums.EntityTypeEnum;
import org.buaa.project.common.enums.MessageTypeEnum;

import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqEvent {

    /**
     * 消息类型
     */
    private MessageTypeEnum messageType;

    /**
     * 触发者ID
     */
    private Long userId;

    /**
     * 触发实体类型
     */
    private EntityTypeEnum entityType;

    /**
     * 触发实体ID
     */
    private Long entityId;

    /**
     * 实体用户ID
     */
    private Long entityUserId;

    /**
     * 产生消息的数据的id
     */
    private Long generateId;

    /**
     * 更多信息
     */
    private HashMap<String, Object> data;

}
