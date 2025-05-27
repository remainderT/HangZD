package org.buaa.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 返回消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRespDTO {
    
    /**
     * 回答数
     */
    private Integer answerCount;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 删除标志
     */
    private Integer delFlag;
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 图片路径
     */
    private String images;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 是否解决
     */
    private Integer solvedFlag;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 浏览量
     */
    private Integer viewCount;
}
