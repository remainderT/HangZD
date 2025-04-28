package org.buaa.project.dto.req.user;

import lombok.Data;

import java.util.List;

/**
 * 用户登录请求参数
 */
@Data
public class AskUsersReqDTO {

    private Long aid;
    private Long qid;
    private List<Long> ids;
}
