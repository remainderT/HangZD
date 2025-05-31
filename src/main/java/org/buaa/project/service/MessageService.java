package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.MessageDO;
import org.buaa.project.dto.req.message.MessageUpdateReqDTO;
import org.buaa.project.dto.req.message.MessageUploadReqDTO;

import java.util.List;

/**
 * 消息接口层
 */
public interface MessageService extends IService<MessageDO> {

    /**
     * 根据id查询消息
     */
    public MessageDO getMessageById(Long id);
    /**
     * 根据发送者id查询消息列表
     */
    public List<MessageDO> getMessagesBySender(Long userId,Long conversationId);

    /**
     * 获取当前用户的消息列表
     */
    public List<MessageDO> getUsersMessages(Long conversationId);
    /**
     * 根据发送者和接收者id查询消息列表
     */
    public List<MessageDO> getMessagesBySenderAndReceiver(Long senderId, Long receiverId,Long conversationId);
    /**
     * 根据会话id查询消息列表
     */
    public List<MessageDO> getMessagesByConvId(Long conversationId);
    /**
     * 添加消息
     */
    public void addMessage(MessageUploadReqDTO requestParam);
    /**
     * 更新消息
     */
    public void updateMessage(MessageUpdateReqDTO requestParam);
    /**
     * 删除消息
     */
    public void deleteMessage(Long id);

    /**
     * 消息按用户创建时间排序
     */
    public List<MessageDO> sortMessagesByCreateTime(Long conversationId);

    /**
     * 已读消息
     */

    public void readMessage(Long id);
}
