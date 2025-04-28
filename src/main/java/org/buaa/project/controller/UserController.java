package org.buaa.project.controller;

import jakarta.servlet.ServletRequest;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.req.user.AskUsersReqDTO;
import org.buaa.project.dto.req.user.UserLoginReqDTO;
import org.buaa.project.dto.req.user.UserRegisterReqDTO;
import org.buaa.project.dto.req.user.UserUpdateReqDTO;
import org.buaa.project.dto.resp.*;
import org.buaa.project.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查找用户信
     */
    @GetMapping("/api/hangzd/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));
    }
    /**
     * 根据用户ID查找用户信息
     */
    @GetMapping("/api/hangzd/user/id/{id}")
    public Result<UserRespDTO> getUserById(@PathVariable("id") Long id) {
        return Results.success(userService.getUserById(id));
    }

    /**
     * 查询用户名是否存在
     */
    @GetMapping("/api/hangzd/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        return Results.success(userService.hasUsername(username));
    }

    /**
     * 注册时候获得验证码
     */
    @GetMapping("/api/hangzd/user/send-code")
    public Result<Boolean> sendCode(@RequestParam("mail") String mail) {
        return Results.success(userService.sendCode(mail));
    }

    /**
     * 注册用户
     */
    @PostMapping("/api/hangzd/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Results.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/api/hangzd/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam, ServletRequest request) {
        return Results.success(userService.login(requestParam, request));
    }

    /**
     * 检查用户是否登录
     */
    @GetMapping("/api/hangzd/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username, @RequestParam("token") String token) {
        return Results.success(userService.checkLogin(username, token));
    }

    /**
     * 用户退出登录
     */
    @DeleteMapping("/api/hangzd/user/logout")
    public Result<Void> logout(@RequestParam("username") String username, @RequestParam("token") String token) {
        userService.logout(username, token);
        return Results.success();
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/api/hangzd/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO requestParam) {
        userService.update(requestParam);
        return Results.success();
    }
    
    /**
     * 更改密码
     */
    @PutMapping("/api/hangzd/user/password")
    public Result<Void> change_password(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        return Results.success();
    }
    
    /**
     * 增加该用户被点赞数
     */
    @PutMapping("/api/hangzd/user/like")
    public Result<Void> likeUser(@RequestParam("username") String username, @RequestParam("increment") Integer increment) {
        userService.likeUser(username, increment);
        return Results.success();
    }

    /**
     * 减少该用户被点赞数
     */
    @PutMapping("/api/hangzd/user/dislike")
    public Result<Void> dislikeUser(@RequestParam("username") String username, @RequestParam("decrement") Integer decrement) {
        userService.dislikeUser(username, decrement);
        return Results.success();
    }

    /**
     * 添加/取消一个收藏
     */
    @PutMapping("/api/hangzd/user/collect")
    public Result<Void> collectUpdate(@RequestParam("collect") Boolean collect) {
        userService.collectUpdate(collect);
        return Results.success();
    }

    /**
     * 增加/减少该用户评论有用数
     */
    @PutMapping("/api/hangzd/user/useful")
    public Result<Void> usefulUpdate(@RequestParam("username") String username, @RequestParam("useful") Boolean useful) {
        userService.usefulUpdate(username,useful);
        return Results.success();
    }

    /**
     * 找回密码
     */
    /*@PostMapping("/api/hangzd/user/findback-password")
    public Result<Void> findbackPassword(@RequestBody UserRegisterReqDTO requestParam) {
        //TODO
        return Results.success();
    }*/

    /**
     * 获取当前用户的所有活跃回答列表
     */
    @GetMapping("/api/hangzd/user/active_answers")
    public Result<List<AnswerRespDTO>> getActiveAnswers(@RequestParam("username") String username) {
        return Results.success(userService.getActiveAnswers(username));
    }

    /**
     * 获取当前用户的所有活跃提问列表
     */
    @GetMapping("/api/hangzd/user/active_questions")
    public Result<List<QuestionRespDTO>> getActiveQuestions(@RequestParam("username") String username) {
        return Results.success(userService.getActiveQuestions(username));
    }
    
    /**
     * 更新当前用户的标签
     */
    @PutMapping("/api/hangzd/user/tags")
    public Result<Void> updateUserTags(@RequestParam("username") String username, @RequestParam String tags) {
        userService.updateTags(username, tags);
        return Results.success();
    }

    /**
     * 向指定的数个用户发出提问
     */
    @PutMapping("/api/hangzd/user/ask")
    public Result<Void> askUsers(@RequestBody AskUsersReqDTO requestParam) {
        System.out.println(requestParam);
        userService.askUsers(requestParam);
        return Results.success();
    }
}

