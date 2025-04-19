package org.buaa.project.dto.req.user;

import lombok.Data;

/**
 * 用户登录请求参数
 */
@Data
public class UserLoginReqDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;
}
