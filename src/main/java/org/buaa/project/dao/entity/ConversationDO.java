package org.buaa.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.buaa.project.common.database.BaseDO;

/**
 * 会话持久层实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conversation")
public class ConversationDO extends BaseDO {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户1ID
     */
    private Long user1;

    /**
     * 用户2ID
     */
    private Long user2;

    /**
     * 状态
     */
    private Integer status;


} 