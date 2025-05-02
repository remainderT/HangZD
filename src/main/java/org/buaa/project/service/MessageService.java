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
    public List<MessageDO> getMessagesBySender(Long userId);

    /**
     * 获取当前用户的消息列表
     */
    public List<MessageDO> getUsersMessages();
    /**
     * 根据发送者和接收者id查询消息列表
     */
    public List<MessageDO> getMessagesBySenderAndReceiver(Long senderId, Long receiverId);

    /**
     * 添加消息
     */
    public void sendMessage(MessageUploadReqDTO requestParam);
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
    public List<MessageDO> sortMessagesByCreateTime();

    /**
     * 已读消息
     */

    public void readMessage(Long id);
}
