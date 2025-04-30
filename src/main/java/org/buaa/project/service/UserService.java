package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.ServletRequest;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.req.user.AskUsersReqDTO;
import org.buaa.project.dto.req.user.UserLoginReqDTO;
import org.buaa.project.dto.req.user.UserRegisterReqDTO;
import org.buaa.project.dto.req.user.UserUpdateReqDTO;
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
     * 根据用户id查询用户信息
     */
    UserRespDTO getUserById(Long id);

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
    void logout(String username, String token);

    /**
     * 更新用户信息
     */
    void update(UserUpdateReqDTO requestParam);
    
    /**
     * 更改密码
     */
    void changePassword(String oldPassword, String newPassword);
    
    /**
     * 增加该用户被点赞数
     */
    void likeUser(String username, Integer increment);

    /**
     * 减少该用户被点赞数
     */
    void dislikeUser(String username, Integer decrement);

    /**
     * 修改该用户收藏题目数
     */
    void collectUpdate(Boolean collect);

    /**
     * 修改用户评论有用数
     */
    void usefulUpdate(String username,Boolean useful);

    /**
     * 获取当前用户的所有活跃回答列表
     */
    List<AnswerRespDTO> getActiveAnswers(String username);

    /**
     * 获取当前用户的所有活跃问题列表
     */
    List<QuestionRespDTO> getActiveQuestions(String username);
    
    /**
     * 更新当前用户的用户标签
     */
    void updateTags(String username, String newTags);

    void askUsers(AskUsersReqDTO requestParam);

    Date getLastActiveTime(Long id);
}
