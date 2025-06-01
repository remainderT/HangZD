package org.buaa.project.controller;

import jakarta.servlet.ServletRequest;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.req.user.*;
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

import java.util.Date;
import java.util.List;


/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查找用户信息
     */
    @GetMapping("/api/hangzd/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));
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
    public Result<Void> logout() {
        userService.logout();
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
    public Result<Void> changePassword(@RequestBody ChangePasswordReqDTO requestParam) {
        userService.changePassword(requestParam);
        return Results.success();
    }
    
    /**
     * 点赞用户
     */
    @PutMapping("/api/hangzd/user/like")
    public Result<Void> likeUser(@RequestBody LikeUserReqDTO requestParam) {
        userService.likeUser(requestParam);
        return Results.success();
    }
    
    /**
     * 更新当前用户的标签
     */
    @PutMapping("/api/hangzd/user/tags")
    public Result<Void> updateUserTags(@RequestBody UpdateUserTagsReqDTO requestParam) {
        userService.updateTags(requestParam);
        return Results.success();
    }

    /**
     * 通过id获取用户信息
     */
    @GetMapping("/api/hangzd/user/id/{id}")
    public Result<UserDO> getUserById(@PathVariable("id") Long id) {
        return Results.success(userService.getUserById(id));
    }

    /**
     * 修改默认公开状态
     */
    @PutMapping("/api/hangzd/user/default-public")
    public Result<Integer> alterDefaultPublic() {
        Integer newState = userService.alterDefaultPublic();
        return Results.success(newState);
    }

    /**
     * 获取当前用户的默认公开状态
     */
    @GetMapping("/api/hangzd/user/default-public")
    public Result<Integer> getDefaultPublic() {
        Integer defaultPublic = userService.getDefaultPublic();
        return Results.success(defaultPublic);
    }

}

