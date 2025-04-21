package org.buaa.project.controller;

import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dto.req.question.QuestionSolveReqDTO;
import org.buaa.project.dto.req.question.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.service.QuestionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/api/astroq/question")
    public Result<Void> uploadQuestion(@RequestBody QuestionUploadReqDTO requestParam) {
        questionService.uploadQuestion(requestParam);
        return Results.success();
    }

    /**
     * 修改问题
     */
    @PutMapping("/api/astroq/question")
    public Result<Void> updateQuestion(@RequestBody QuestionUpdateReqDTO requestParam) {
        questionService.updateQuestion(requestParam);
        return Results.success();
    }

    /**
     * 删除问题
     */
    @DeleteMapping("/api/astroq/question")
    public Result<Void> deleteQuestion(@RequestParam("id") Long Id) {
        questionService.deleteQuestion(Id);
        return Results.success();
    }

    /**
     * 标记问题已经解决
     */
    @PostMapping("/api/astroq/question/solve")
    public Result<Void> resolvedQuestion(@RequestBody QuestionSolveReqDTO requestParam) {
        questionService.resolvedQuestion(requestParam);
        return Results.success();
    }
}

