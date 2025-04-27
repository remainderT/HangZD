package org.buaa.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRespDTO {
    /**
     * 回答id
     */
    private Long id;
    /**
     * 发布人id
     */
    private Long user_id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 对应问题的id
     */
    private Long question_id;
    /**
     * 内容
     */
    private String content;
    /**
     * 包含的图片
     */
    private String images;
    /**
     * 点赞数
     */
    private Integer like_count;
    /**
     * 是否有用
     */
    private Boolean is_useful;
    /**
     * 创建时间
     */
    private String create_time;
    /**
     * 更新时间
     */
    private String update_time;
    /**
     * 是否被删除
     */
    private Integer del_flag;
}
