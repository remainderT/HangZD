package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.convention.exception.ServiceException;
import org.buaa.project.dao.entity.MessageDO;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.mapper.MessageMapper;
import org.buaa.project.dto.req.message.MessageUpdateReqDTO;
import org.buaa.project.dto.req.message.MessageUploadReqDTO;
import org.buaa.project.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static org.buaa.project.common.enums.MessageErrorCodeEnum.MESSAGE_NOT_FOUND;
import static org.buaa.project.common.enums.MessageErrorCodeEnum.MESSAGE_SENDER_INCORRECT;

/**
 * 消息接口实现层
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, MessageDO> implements MessageService {


    public boolean existsMessage(Long id) {
        return (baseMapper.selectById(id) != null);
    }

    @Override
    public MessageDO getMessageById(Long id) {
        MessageDO message = baseMapper.selectById(id);
        if (Objects.isNull(message)) {
            throw new ServiceException(MESSAGE_NOT_FOUND);
        }
        return message;
    }

    @Override
    public List<MessageDO> getMessagesBySender(Long userId) {
        LambdaQueryWrapper<MessageDO> queryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                .eq(MessageDO::getToId, userId)
                .eq(MessageDO::getDelFlag, 0)
                .orderByDesc(MessageDO::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<MessageDO> getUsersMessages(){
        LambdaQueryWrapper<MessageDO> queryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                .eq(MessageDO::getDelFlag, 0)
                .orderByDesc(MessageDO::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<MessageDO> getMessagesBySenderAndReceiver(Long senderId, Long receiverId){
        LambdaQueryWrapper<MessageDO> queryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                .eq(MessageDO::getFromId, senderId)
                .eq(MessageDO::getToId, receiverId)
                .eq(MessageDO::getDelFlag, 0)
                .orderByDesc(MessageDO::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void addMessage(MessageUploadReqDTO requestParam) {
        MessageDO message = BeanUtil.toBean(requestParam, MessageDO.class);
        message.setFromId(UserContext.getUserId());
        message.setStatus(0);//默认未读
        message.setDelFlag(0);//默认未删除
        baseMapper.insert(message);
    }

    @Override
    public void updateMessage(MessageUpdateReqDTO requestParam) {
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
    public List<MessageDO> sortMessagesByCreateTime() {
        LambdaQueryWrapper<MessageDO> queryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                .eq(MessageDO::getFromId, UserContext.getUserId())
                .eq(MessageDO::getDelFlag, 0)
                .orderByDesc(MessageDO::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void readMessage(Long id) {
        if (!existsMessage(id)) {
            throw new ServiceException(MESSAGE_NOT_FOUND);
        }
        MessageDO message = baseMapper.selectById(id);
        message.setStatus(1); // 设置为已读
        baseMapper.updateById(message);
    }
}
