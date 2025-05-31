package org.buaa.project.dto.req.conversation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.buaa.project.dao.entity.ConversationDO;

/**
 * 公开的会话分页查询请求参数
 */
@Data
public class ConversationPageReqDTO extends Page<ConversationDO> {

    /**
     * 搜索词
     */
    private String keyword;

}
