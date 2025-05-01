package org.buaa.project.dto.req.user;

import lombok.Data;

/**
 * 更改密码请求参数
 */
@Data
public class ChangePasswordReqDTO {

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
} 