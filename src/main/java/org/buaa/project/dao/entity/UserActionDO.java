package org.buaa.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.buaa.project.common.database.BaseDO;

import java.util.Date;

/**
 * 用户行为实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_action")
public class UserActionDO extends BaseDO {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 实体类型（user、question、answer）
     */
    private String entityType;
    
    /**
     * 实体ID
     */
    private Long entityId;
    
    /**
     * 收藏状态：0-未收藏，1-已收藏
     */
    private Integer collectStat;
    
    /**
     * 点赞状态：0-未点赞，1-已点赞
     */
    private Integer likeStat;
    
    /**
     * 上次浏览时间
     */
    private Date lastViewTime;
} 