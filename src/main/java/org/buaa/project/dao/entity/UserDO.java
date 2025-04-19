package org.buaa.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import org.buaa.project.common.database.BaseDO;

/**
 * 用户持久层实体
 */
@Data
@Builder
@TableName("user")
public class UserDO extends BaseDO {
    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 盐
     */
    private String salt;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 个人简介
     */
    private String introduction;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏题目数
     */
    private Integer collectCount;

    /**
     * 评论有用数
     */
    private Integer usefulCount;

    /**
     * 用户类型
     */
    private String userType;


}
