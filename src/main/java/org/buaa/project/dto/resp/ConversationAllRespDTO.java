package org.buaa.project.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 公开会话分页查询响应
 */
@Data
@Builder
public class ConversationAllRespDTO {

    Long total;

    Long size;

    Long current;

    List<ConversationPageRespDTO> records;

}
