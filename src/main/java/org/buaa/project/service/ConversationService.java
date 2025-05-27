package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.ConversationDO;
import org.buaa.project.dto.req.conversation.ConversationCreateReqDTO;

import java.util.List;

/**
 * 会话接口层
 */
public interface ConversationService extends IService<ConversationDO> {

    /**
     * 获取当前用户的会话列表
     */
    public List<ConversationDO> getUserConversations();

    /**
     * 创建会话
     */
    public Long createConversation(ConversationCreateReqDTO requestParam);

    /**
     * 删除会话
     */
    public void deleteConversation(Long id);
} 