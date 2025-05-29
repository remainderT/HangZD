package org.buaa.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.enums.EntityTypeEnum;
import org.buaa.project.common.enums.UserActionTypeEnum;
import org.buaa.project.dao.entity.*;
import org.buaa.project.dao.mapper.*;
import org.buaa.project.service.UserActionService;
import org.buaa.project.toolkit.RedisCount;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.buaa.project.common.consts.RedisCacheConstants.*;

/**
 * 用户行为服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserActionServiceImpl extends ServiceImpl<UserActionMapper, UserActionDO> implements UserActionService {

    private final RedisCount redisCount;

    private final RedissonClient redissonClient;

    private final UserMapper userMapper;

    private final QuestionMapper questionMapper;

    //private final AnswerMapper answerMapper;

    private final ConversationMapper conversationMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserActionDO getUserAction(Long userId, EntityTypeEnum entityType, Long entityId) {
        LambdaQueryWrapper<UserActionDO> queryWrapper = Wrappers.lambdaQuery(UserActionDO.class)
                .eq(UserActionDO::getUserId, userId)
                .eq(UserActionDO::getEntityId, entityId)
                .eq(UserActionDO::getEntityType, entityType);

        UserActionDO userActionDO =  baseMapper.selectOne(queryWrapper);
        if (userActionDO == null) {
            userActionDO = UserActionDO.builder()
                    .userId(userId)
                    .entityType(entityType.type())
                    .entityId(entityId)
                    .likeStat(0)
                    .collectStat(0)
                    .recommendStat(0)
                    .build();
            baseMapper.insert(userActionDO);
        }
        return userActionDO;
    }

    @Override
    @Transactional
    public void collectAndLike(EntityTypeEnum entityType, Long entityId, UserActionTypeEnum actionType) {
        boolean isPositive = false;
        Long userId = UserContext.getUserId();

        RLock lock = redissonClient.getLock(USER_ACTION_KEY + userId + "_" + entityId);
        lock.lock();
        try {
            UserActionDO userAction = getUserAction(userId, entityType, entityId);
            UserDO userFrom;
            UserDO userTo;
            //AnswerDO answer;
            ConversationDO conversation;
            QuestionDO question;
            Long entityUserId;
            switch (actionType) {
                case LIKE:
                    isPositive = userAction.getLikeStat() == 0;
                    userAction.setLikeStat(isPositive ? 1 : 0);
                    if (entityType == EntityTypeEnum.QUESTION) {
                        question = questionMapper.selectById(entityId);
                        question.setLikeCount(question.getLikeCount() + (isPositive ? 1 : -1));
                        questionMapper.updateById(question);
                        redisCount.hIncr(QUESTION_COUNT_KEY + entityId, "like", isPositive ? 1 : -1);
                        if (isPositive) {
                            stringRedisTemplate.opsForSet().add(QUESTION_LIKE_SET_KEY + entityId, userId.toString());
                        } else {
                            stringRedisTemplate.opsForSet().remove(QUESTION_LIKE_SET_KEY + entityId, userId.toString());
                        }
                        entityUserId = question.getUserId();
                    } else {
                        conversation = conversationMapper.selectById(entityId);
                        conversation.setLikeCount(conversation.getLikeCount() + (isPositive ? 1 : -1));
                        conversationMapper.updateById(conversation);
                        redisCount.hIncr(ANSWER_COUNT_KEY + entityId, "like", isPositive ? 1 : -1);
                        if (isPositive) {
                            stringRedisTemplate.opsForSet().add(ANSWER_LIKE_SET_KEY + entityId, userId.toString());
                        } else {
                            stringRedisTemplate.opsForSet().remove(ANSWER_LIKE_SET_KEY + entityId, userId.toString());
                        }
                        entityUserId = conversation.getUser2();
                    }
                    userTo = userMapper.selectById(entityUserId);
                    userTo.setLikeCount(userTo.getLikeCount() + (isPositive ? 1 : -1));
                    userMapper.updateById(userTo);
                    redisCount.hIncr(USER_COUNT_KEY + entityUserId, "like", isPositive ? 1 : -1);
                    break;

                case COLLECT:
                    isPositive = userAction.getCollectStat() == 0;
                    userAction.setCollectStat(isPositive ? 1 : 0);
                    userFrom = userMapper.selectById(userId);
                    userFrom.setCollectCount(userFrom.getCollectCount() + (isPositive ? 1 : -1));
                    userMapper.updateById(userFrom);
                    redisCount.hIncr(USER_COUNT_KEY + userId, "collect", isPositive ? 1 : -1);
                    if (isPositive) {
                        stringRedisTemplate.opsForSet().add(QUESTION_COLLECT_SET_KEY + userId, entityId.toString());
                    } else {
                        stringRedisTemplate.opsForSet().remove(QUESTION_COLLECT_SET_KEY + userId, entityId.toString());
                    }
                    break;

                default:
                    break;
            }
            baseMapper.updateById(userAction);

        } finally {
            lock.unlock();
        }

    }

    @Override
    public void recommendQuestion(Long questionId, Long userId) {
        UserActionDO userAction = getUserAction(userId, EntityTypeEnum.QUESTION, questionId);
        userAction.setRecommendStat(1);
        baseMapper.updateById(userAction);
    }
}