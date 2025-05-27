package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.convention.exception.ServiceException;
import org.buaa.project.dao.entity.ConversationDO;
import org.buaa.project.dao.entity.MessageDO;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.ConversationMapper;
import org.buaa.project.dao.mapper.MessageMapper;
import org.buaa.project.dao.mapper.UserMapper;
import org.buaa.project.dto.req.message.MessageUpdateReqDTO;
import org.buaa.project.dto.req.message.MessageUploadReqDTO;
import org.buaa.project.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static org.buaa.project.common.enums.MessageErrorCodeEnum.*;

//import org.buaa.project.service.UserService;


/**
 * 消息接口实现层
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, MessageDO> implements MessageService {
    //private final UserService userService;
    private final UserMapper userMapper;
    private final ConversationMapper conversationMapper;

    public boolean existsMessage(Long id) {
        MessageDO message = baseMapper.selectById(id);
        if (Objects.isNull(message)) {
            return false;
        }
        // 检查是否已删除
        if (message.getDelFlag() == 1) {
            return false;
        }
        return true;
    }

    @Override
    public MessageDO getMessageById(Long id) {
        MessageDO message = baseMapper.selectById(id);
        if (Objects.isNull(message)) {
            throw new ServiceException(MESSAGE_NOT_FOUND);
        }
        // 检查是否已删除
        if (message.getDelFlag() == 1) {
            throw new ServiceException(MESSAGE_NOT_FOUND);
        }
        return message;
    }

    @Override
    public List<MessageDO> getMessagesBySender(Long userId,Long conversationId) {
        LambdaQueryWrapper<MessageDO> queryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                .eq(MessageDO::getFromId, userId)
                .eq(MessageDO::getConversationId,conversationId)
                .eq(MessageDO::getDelFlag, 0)
                .orderByDesc(MessageDO::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<MessageDO> getUsersMessages(Long conversationId){
        LambdaQueryWrapper<MessageDO> queryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                .eq(MessageDO::getConversationId , conversationId)
                .eq(MessageDO::getDelFlag, 0)
                .orderByDesc(MessageDO::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<MessageDO> getMessagesBySenderAndReceiver(Long senderId, Long receiverId,Long conversationId){
        LambdaQueryWrapper<MessageDO> queryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                .eq(MessageDO::getFromId, senderId)
                .eq(MessageDO::getToId, receiverId)
                .eq(MessageDO::getConversationId, conversationId)
                .eq(MessageDO::getDelFlag, 0)
                .orderByDesc(MessageDO::getCreateTime);
        //更新状态位
        List<MessageDO> messages = baseMapper.selectList(queryWrapper);
        for( MessageDO message : messages) {
            if (message.getStatus() == 0 && Objects.equals(UserContext.getUserId(), message.getToId())) {
                message.setStatus(1); // 设置为已读
                baseMapper.updateById(message);
            }
        }
        return messages;
    }

    @Override
    public void addMessage(MessageUploadReqDTO requestParam) {
        checkMessageValid(requestParam);
        MessageDO message = BeanUtil.toBean(requestParam, MessageDO.class);
        message.setFromId(requestParam.getFromId());
        message.setStatus(0);//默认未读
        message.setDelFlag(0);//默认未删除
        message.setConversationId(requestParam.getConversationId());
        //message.setCreateTime();
        message.setGenerateId(0L);
        baseMapper.insert(message);
    }

    @Override
    public void updateMessage(MessageUpdateReqDTO requestParam) {
        //checkMessageValid(requestParam);
        /*MessageDO message = BeanUtil.toBean(requestParam, MessageDO.class);
        if (!existsMessage(message.getId())) {
            throw new ServiceException(MESSAGE_NOT_FOUND);
        }
        baseMapper.updateById(message);*/
    }

    @Override
    public void deleteMessage(Long id) {
        if (!existsMessage(id)) {
            throw new ServiceException(MESSAGE_NOT_FOUND);
        }
        //检查当前用户是否是消息发起者
        MessageDO message = baseMapper.selectById(id);
        if (!Objects.equals(UserContext.getUserId(), message.getFromId())) {
            throw new ServiceException(MESSAGE_SENDER_INCORRECT);
        }
        message.setDelFlag(1); // 设置为已删除
        baseMapper.updateById(message);
    }

    @Override
    public List<MessageDO> sortMessagesByCreateTime(Long conversationId) {
        LambdaQueryWrapper<MessageDO> queryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                .eq(MessageDO::getFromId, UserContext.getUserId())
                .eq(MessageDO::getConversationId, conversationId)
                .eq(MessageDO::getDelFlag, 0)
                .orderByDesc(MessageDO::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void readMessage(Long id) {
        /*if (!existsMessage(id)) {
            throw new ServiceException(MESSAGE_NOT_FOUND);
        }
        MessageDO message = baseMapper.selectById(id);
        message.setStatus(1); // 设置为已读
        baseMapper.updateById(message);*/
    }

    public void checkMessageValid(MessageUploadReqDTO requestParam) {
        //System.out.println(requestParam);
        //检查消息内容是否为空
        if (requestParam.getContent() == null || requestParam.getContent().isEmpty()) {
            throw new ServiceException(MESSAGE_EMPTY_MESSAGE);
        }
        //查看会话是否存在
        Long conversationId = requestParam.getConversationId();
        ConversationDO conversation = conversationMapper.selectById(conversationId);
        if(conversation == null) {
            throw new ServiceException(CONVERSATION_NOT_FOUND);
        }

        //检查消息发送者是否是本人
        if(requestParam.getFromId() == null || (requestParam.getFromId() != 0 && !requestParam.getFromId().equals(UserContext.getUserId()))) {
            throw new ServiceException(MESSAGE_SENDER_INCORRECT);
        }
        //检查消息接收者是否存在且不是系统
        if(requestParam.getToId() == 0){
            throw new ServiceException(MESSAGE_RECEIVER_ILLEGAL);
        }
        UserDO user = userMapper.selectById(requestParam.getToId());
        if(user == null ) {
            throw new ServiceException(MESSAGE_RECEIVER_ILLEGAL);
        }
    }

    public void checkMessageValid(MessageUpdateReqDTO requestParam) {
        //TODO：

    }
}
