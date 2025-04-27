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
public class QuestionRespDTO {
    /**
     *  消息id
     */
    private Long id;
    /**
     * 分类ID
     */
    private long category_Id;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 发布人ID
     */
    private Long user_Id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 照片路径
     */
    private String images;
    /**
     * 浏览量
     */
    private Integer view_count;
    /**
     * 创建时间
     */
    private String create_time;

}
