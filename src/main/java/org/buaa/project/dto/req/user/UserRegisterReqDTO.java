package org.buaa.project.dto.req.user;

import lombok.Data;

/**
 * 用户注册请求参数
 */
@Data
public class UserRegisterReqDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 验证码
     */
    private String code;
}