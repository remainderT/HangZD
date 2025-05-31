package org.buaa.project.dto.resp;

import lombok.Data;

import java.util.List;

/**
 * 用户信息返回参数响应
 */
@Data
public class UserRecommendedRespDTO {
    
    /**
     * 活跃天数
     */
    private Integer active_days;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 历史回答
     */
    
    private List<String> history_replies;
    
    /**
     * id
     */
    private Long id;
    
    /**
     * 点赞数
     */
    private Integer like_count;
    
    /**
     * 用户标签
     */
    private List<String> tags;
    
    /**
     * 被认为回答有用数
     */
    private Integer userful_count;
    
    /**
     * 用户名
     */
    private String username;
    
}
