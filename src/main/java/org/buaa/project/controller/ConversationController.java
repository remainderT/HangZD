package org.buaa.project.controller;

import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dao.entity.ConversationDO;
import org.buaa.project.dto.req.conversation.ConversationCreateReqDTO;
import org.buaa.project.service.ConversationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话控制层
 */
@RestController
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 创建会话
     */
    @PostMapping("/api/hangzd/conversation")
    public Result<Long> createConversation(@RequestBody ConversationCreateReqDTO requestParam) {
        Long cid = conversationService.createConversation(requestParam);
        return Results.success(cid);
    }

    /**
     * 分页获取当前用户的会话列表
     */
    @GetMapping("/api/hangzd/conversations")
    public Result<List<ConversationDO>> getUserConversations() {
        List<ConversationDO> conversations = conversationService.getUserConversations();
        return Results.success(conversations);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/api/hangzd/conversation/{id}")
    public Result<Void> deleteConversation(@PathVariable Long id) {
        conversationService.deleteConversation(id);
        return Results.success();
    }

    /**
     * 设置是否公开选项
     */
    @PutMapping("/api/hangzd/conversation/{id}/public")
    public Result<Integer> setConversationPublic(@PathVariable Long id, @RequestParam Boolean isPublic) {
        Integer newStatus = conversationService.setConversationPublic(id, isPublic);
        return Results.success(newStatus);
    }

    /**
     * 结束该会话
     */
    @PutMapping("/api/hangzd/conversation/{id}/end")
    public Result<Void> endConversation(@PathVariable Long id) {
        conversationService.endConversation(id);
        return Results.success();
    }

    /**
     * 获取所有已公开会话
     */
    @GetMapping("/api/hangzd/conversations/public")
    public Result<List<ConversationDO>> getPublicConversations() {
        List<ConversationDO> publicConversations = conversationService.getPublicConversations();
        return Results.success(publicConversations);
    }
}
