package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.common.enums.EntityTypeEnum;
import org.buaa.project.common.enums.UserActionTypeEnum;
import org.buaa.project.dao.entity.UserActionDO;

/**
 * 用户行为服务接口
 */
public interface UserActionService extends IService<UserActionDO> {

    /**
     * 检查用户行为是否存在
     */
    UserActionDO getUserAction(Long userId, EntityTypeEnum entityType, Long entityId);

    /**
     * 用户行为： 点赞、收藏
     */
    void collectAndLike(EntityTypeEnum entityType, Long entityId, UserActionTypeEnum actionType);

    /**
     * 用户行为： 推荐问题
     */
    void recommendQuestion(Long questionId, Long userId);
    /**
     * 用户行为： 更新浏览记录
     */
    void browse(Long userId,Long conversationId);

} 