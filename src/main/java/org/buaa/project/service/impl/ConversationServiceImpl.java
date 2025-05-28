package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.convention.exception.ServiceException;
import org.buaa.project.dao.entity.ConversationDO;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.ConversationMapper;
import org.buaa.project.dao.mapper.UserMapper;
import org.buaa.project.dto.req.conversation.ConversationCreateReqDTO;
import org.buaa.project.service.ConversationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static org.buaa.project.common.enums.MessageErrorCodeEnum.*;

/**
 * 会话接口实现层
 */
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, ConversationDO> implements ConversationService {

    private final UserMapper userMapper;
    /**
     * 检查指定问题的会话是否存在
     */
    private boolean existsConversation(Long id) {
        ConversationDO conversation = baseMapper.selectById(id);
        if (Objects.isNull(conversation)) {
            return false;
        }
        if (conversation.getDelFlag() == 1) {
            return false;
        }
        
        // 根据当前用户判断对话是否对其可见
        Long currentUserId = UserContext.getUserId();
        // display_status = 1 表示 user1 删除，display_status = 3 表示都删除
        if (Objects.equals(currentUserId, conversation.getUser1()) &&
            (conversation.getDisplayStatus() == 1 || conversation.getDisplayStatus() == 3)) {
            return false;
        }
        // display_status = 2 表示 user2 删除，display_status = 3 表示都删除
        if (Objects.equals(currentUserId, conversation.getUser2()) &&
            (conversation.getDisplayStatus() == 2 || conversation.getDisplayStatus() == 3)) {
            return false;
        }
        
        return true;
    }

    /**
     * 根据两个用户ID与问题id查询会话
     */
    private ConversationDO getConversationByUserIds(Long user1Id, Long user2Id, Long questionId) {
        LambdaQueryWrapper<ConversationDO> queryWrapper = Wrappers.lambdaQuery(ConversationDO.class)
                .and(wrapper -> wrapper
                        .and(w -> w
                                .eq(ConversationDO::getUser1, user1Id)
                                .eq(ConversationDO::getUser2, user2Id))
                        .or(w -> w
                                .eq(ConversationDO::getUser1, user2Id)
                                .eq(ConversationDO::getUser2, user1Id)))
                .eq(ConversationDO::getDelFlag, 0)
                .eq(ConversationDO::getQuestionId, questionId);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<ConversationDO> getUserConversations() {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<ConversationDO> queryWrapper = Wrappers.lambdaQuery(ConversationDO.class)
                .and(wrapper -> wrapper
                        .and(w -> w
                                .eq(ConversationDO::getUser1, userId)
                                .notIn(ConversationDO::getDisplayStatus, 1, 3)) // user1不应该看到状态为1或3的对话
                        .or(w -> w
                                .eq(ConversationDO::getUser2, userId)
                                .notIn(ConversationDO::getDisplayStatus, 2, 3))) // user2不应该看到状态为2或3的对话
                .eq(ConversationDO::getDelFlag, 0)
                .orderByDesc(ConversationDO::getUpdateTime);
        List<ConversationDO> list = baseMapper.selectList(queryWrapper);
        System.out.println("Here:"+list);
        return list;
    }

    @Override
    public Long createConversation(ConversationCreateReqDTO requestParam) {
        Long currentUserId = UserContext.getUserId();
        Long user2Id = requestParam.getUser2();
        Long questionId = requestParam.getQuestionId();
        
        // 检查是否已存在会话
        ConversationDO existingConversation = getConversationByUserIds(currentUserId, user2Id, questionId);
        if (existingConversation != null) {
            throw new ServiceException(CONVERSATION_ALREADY_EXISTS);
        }
        
        ConversationDO conversation = BeanUtil.toBean(requestParam, ConversationDO.class);
        conversation.setUser1(currentUserId);
        conversation.setUser2(user2Id);
        conversation.setQuestionId(questionId);
        UserDO user2 = userMapper.selectById(user2Id);
        conversation.setDefaultPublic(user2.getDefaultPublic());
        //System.out.println(conversation);
        baseMapper.insert(conversation);
        return conversation.getId();
    }

    @Override
    public void deleteConversation(Long id) {
        if (!existsConversation(id)) {
            throw new ServiceException(CONVERSATION_NOT_FOUND);
        }
        
        // 检查当前用户是否是会话参与者
        ConversationDO conversation = baseMapper.selectById(id);
        Long currentUserId = UserContext.getUserId();
        
        if (!Objects.equals(currentUserId, conversation.getUser1()) &&
            !Objects.equals(currentUserId, conversation.getUser2())) {
            throw new ServiceException(CONVERSATION_ACCESS_DENIED);
        }
        
        // 更新会话状态
        ConversationDO updateConversation = new ConversationDO();
        updateConversation.setId(id);
        
        Integer currentStatus = conversation.getDisplayStatus();
        Integer newStatus = currentStatus;
        
        // 根据当前用户和当前状态，设置新状态
        if (Objects.equals(currentUserId, conversation.getUser1())) {
            if (currentStatus == 0) {
                newStatus = 1; // user1删除
            } else if (currentStatus == 2) {
                newStatus = 3; // user2已删除，现在user1也删除
            }
        } else { // currentUserId equals user2
            if (currentStatus == 0) {
                newStatus = 2; // user2删除
            } else if (currentStatus == 1) {
                newStatus = 3; // user1已删除，现在user2也删除
            }
        }
        
        updateConversation.setDisplayStatus(newStatus);
        
        // 如果状态为3（都删除），则可以物理删除
        if (newStatus == 3) {
            updateConversation.setDelFlag(1);
        }
        
        baseMapper.updateById(updateConversation);
    }

    @Override
    public Integer setConversationPublic(Long conversationId,Boolean isPublic){
        if (!existsConversation(conversationId)) {
            throw new ServiceException(CONVERSATION_NOT_FOUND);
        }

        // 检查当前用户是否是会话参与者
        ConversationDO conversation = baseMapper.selectById(conversationId);
        Long currentUserId = UserContext.getUserId();

        if (!Objects.equals(currentUserId, conversation.getUser1()) &&
            !Objects.equals(currentUserId, conversation.getUser2())) {
            throw new ServiceException(CONVERSATION_ACCESS_DENIED);
        }

        if(conversation.getStatus() != 1) {
            throw new ServiceException(CONVERSATION_STATE_ERROR);
        }
        // 更新会话的公开状态

        if(Objects.equals(conversation.getUser1(), UserContext.getUserId())) {
            if(isPublic && (getAnswererPublicity(conversation) == 1)) {
                //更新status 为2(双方都同意公开)
                conversation.setStatus(2);
            }
        } else {
            if(isPublic) {
                conversation.setAnswererPublic(1);
            } else {
                conversation.setAnswererPublic(0);
            }

        }

        baseMapper.updateById(conversation);
        return conversation.getStatus();
    }

    public Integer getAnswererPublicity(ConversationDO conversationDO) {
        if(conversationDO.getAnswererPublic() == null) {
            return conversationDO.getDefaultPublic();
        } else {
            return conversationDO.getAnswererPublic();
        }
    }

    @Override
    public void endConversation(Long id) {
        if (!existsConversation(id)) {
            throw new ServiceException(CONVERSATION_NOT_FOUND);
        }

        // 检查当前用户是否是会话发起者
        ConversationDO conversation = baseMapper.selectById(id);
        Long currentUserId = UserContext.getUserId();

        if (!Objects.equals(currentUserId, conversation.getUser1())) {
            throw new ServiceException(CONVERSATION_ACCESS_DENIED);
        }
        if (conversation.getStatus() != 0) {
            throw new ServiceException(CONVERSATION_STATE_ERROR);
        }

        // 更新会话状态为结束
        conversation.setStatus(1); // 0表示已结束
        baseMapper.updateById(conversation);
    }
} 