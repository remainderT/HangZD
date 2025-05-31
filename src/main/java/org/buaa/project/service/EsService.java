package org.buaa.project.service;

import org.buaa.project.dao.entity.ConversationDO;
import org.buaa.project.dto.req.conversation.ConversationPageReqDTO;
import org.buaa.project.dto.resp.ConversationAllRespDTO;

import java.util.List;

/**
 * es服务接口层
 */
public interface EsService {

    /**
     * 关键字分页搜索已经公开的会话
     */
    ConversationAllRespDTO search(ConversationPageReqDTO requestParam);

    /**
     * 自动补全
     */
    List<String> autoComplete(String keyword);

    /**
     * 分词
     */
    List<String> analyze(String text);

    /**
     * 插入文档
     */
    void insert(ConversationDO conversationDO);
}
