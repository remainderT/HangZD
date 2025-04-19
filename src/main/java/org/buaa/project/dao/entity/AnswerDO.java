package org.buaa.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.buaa.project.common.database.BaseDO;

/**
 * 回答持久层实体
 */
@Data
@TableName("answer")
public class AnswerDO extends BaseDO {

    /**
     * ID - unique identifier for each comment
     */
    private Long id;

    /**
     * user_id - the ID of the user who posted the comment
     */
    private Long userId;

    /**
     * username - the username of the user who posted the comment
     */
    private String username;

    /**
     * question_id - the ID of the question this comment responds to
     */
    private Long questionId;

    /**
     * content - the content of the comment, with a maximum length of 1024 characters
     */
    private String content;

    /**
     * images - paths to images associated with the comment, separated by commas, with a maximum of 9 images
     */
    private String images;

    /**
     * useful - indicates if the comment is marked as useful, 1 for true, 0 for false
     */
    private Integer useful;

}
