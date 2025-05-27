package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.ServletRequest;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.req.user.*;
import org.buaa.project.dto.resp.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     */
    Boolean hasUsername(String username);

    /**
     * 发送验证码
     */
    Boolean sendCode(String mail);

    /**
     * 注册用户
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 用户登录
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam, ServletRequest request);

    /**
     * 检查用户是否登录
     */
    Boolean checkLogin(String username, String token);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 更新用户信息
     */
    void update(UserUpdateReqDTO requestParam);
    
    /**
     * 更改密码
     */
    void changePassword(ChangePasswordReqDTO requestParam);
    
    /**
     * 点赞用户
     */
    void likeUser(LikeUserReqDTO requestParam);

    /**
     * 更新当前用户的用户标签
     */
    void updateTags(UpdateUserTagsReqDTO requestParam);

    /**
    * 通过id获取用户信息
    */
    UserDO getUserById(Long id);
}
