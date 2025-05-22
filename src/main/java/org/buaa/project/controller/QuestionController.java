package org.buaa.project.controller;

import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dto.req.question.QuestionSolveReqDTO;
import org.buaa.project.dto.req.question.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.req.question.AskUsersReqDTO;
import org.buaa.project.dto.resp.QuestionRespDTO;
import org.buaa.project.dto.resp.QuestionUploadRespDTO;
import org.buaa.project.dto.resp.UserRespDTO;
import org.buaa.project.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 问题管理控制层
 */
@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 上传问题
     */
    @PostMapping("/api/hangzd/question")
    public Result<QuestionUploadRespDTO> uploadQuestion(@RequestBody QuestionUploadReqDTO requestParam) {
        return Results.success(questionService.uploadQuestion(requestParam));
    }

    /**
     * 修改问题
     */
    @PutMapping("/api/hangzd/question")
    public Result<Void> updateQuestion(@RequestBody QuestionUpdateReqDTO requestParam) {
        questionService.updateQuestion(requestParam);
        return Results.success();
    }

    /**
     * 删除问题
     */
    @DeleteMapping("/api/hangzd/question")
    public Result<Void> deleteQuestion(@RequestParam("id") Long Id) {
        questionService.deleteQuestion(Id);
        return Results.success();
    }

    /**
     * 标记问题已经解决
     */
    @PostMapping("/api/hangzd/question/solve")
    public Result<Void> resolvedQuestion(@RequestBody QuestionSolveReqDTO requestParam) {
        questionService.resolvedQuestion(requestParam);
        return Results.success();
    }

    /**
     * 返回指定id问题的详细信息
     */
    @GetMapping("/api/hangzd/question/{id}")
    public Result<QuestionRespDTO> getQuestionById(@PathVariable("id") Long id) {
        return Results.success(questionService.getQuestionById(id));
    }

    /**
     * 提供问题获取推荐回答的用户信息
     */
    @GetMapping("/api/user/findAnswerers")
    public Result<List<UserRespDTO>> findAnswerers(@RequestParam("questionId") Long questionId) {
        return Results.success(questionService.findAnswerers(questionId));
    }

    /**
     * 获取当前用户的提问列表
     */
    @GetMapping("/api/hangzd/questions/my")
    public Result<List<QuestionRespDTO>> getActiveQuestions() {
        return Results.success(new ArrayList<>());
    }

    /**
     * 向指定的数个用户发出提问
     */
    @PutMapping("/api/hangzd/user/ask")
    public Result<Void> askUsers(@RequestBody AskUsersReqDTO requestParam) {
        questionService.askUsers(requestParam);
        return Results.success();
    }
}

