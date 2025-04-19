package org.buaa.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.buaa.project.common.database.BaseDO;

/**
 * 问题持久层实体
 */
@Data
@TableName("question")
public class QuestionDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 发布人id
     */
    private Long userId;

    /**
     * 发布人用户名
     */
    private String username;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 是否解决 0：未解决 1：已解决
     */
    private Integer solvedFlag;

    /**
     * 包含的图片
     */
    private String images;
}
