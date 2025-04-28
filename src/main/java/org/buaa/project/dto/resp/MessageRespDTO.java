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
public class MessageRespDTO {

    /**
     *  消息id
     */
    private Long id;
    /**
     *  消息发送者id
     */
    private Long fromId;
    /**
     *  消息接收者id
     */
    private Long toId;
    /**
     *  消息類型
     */
    private String type;
    /**
     *  消息內容
     */
    private String content;
    /**
     * 創建時間
     */
    private String createTime;
}
