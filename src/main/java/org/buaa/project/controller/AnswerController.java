package org.buaa.project.controller;

import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dao.entity.AnswerDO;
import org.buaa.project.dao.entity.MessageDO;
import org.buaa.project.dto.req.answer.AnswerUpdateReqDTO;
import org.buaa.project.dto.req.answer.AnswerUploadReqDTO;
import org.buaa.project.dto.req.message.MessageUpdateReqDTO;
import org.buaa.project.dto.req.message.MessageUploadReqDTO;
import org.buaa.project.dto.resp.AnswerRespDTO;
import org.buaa.project.service.AnswerService;
import org.buaa.project.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制层
 */
@RestController
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    /**
     * 添加回答
     */
    @PostMapping("/api/hangzd/answer")
    public Result<Void> postAnswer(@RequestBody AnswerUploadReqDTO message) {
        answerService.postAnswer(message);
        return Results.success();
    }

    /**
     * 根据ID获取回答
     */
    @GetMapping("/api/hangzd/answer/{id}")
    public Result<AnswerRespDTO> getAnswerById(@PathVariable Long id) {
        return Results.success(answerService.getAnswerById(id));
    }

    /**
     * 修改回答
     */
    @PutMapping("/api/hangzd/answer")
    public Result<Void> updateAnswer(@RequestBody AnswerUpdateReqDTO requestParam) {
        answerService.updateAnswer(requestParam);
        return Results.success();
    }


    /**
     * 删除回答
     */
    @DeleteMapping("/api/hangzd/answer")
    public Result<Void> deleteAnswer(@RequestParam("id") Long Id) {
        answerService.deleteAnswer(Id);
        return Results.success();
    }
}
