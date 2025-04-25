package org.buaa.project.controller;

import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dao.entity.MessageDO;
import org.buaa.project.dto.req.message.MessageUpdateReqDTO;
import org.buaa.project.dto.req.message.MessageUploadReqDTO;
import org.buaa.project.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制层
 */
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 根据ID获取消息
     */
    @GetMapping("/api/hangzd/message/{id}")
    public Result<MessageDO> getMessageById(@PathVariable Long id) {
        return Results.success(messageService.getMessageById(id));
    }

    /**
     * 获取当前用户的消息列表
     */
    @GetMapping("/api/hangzd/messages")
    public Result<List<MessageDO>> getUsersMessages() {
        return Results.success(messageService.getUsersMessages());
    }

    /**
     * 根据发送者ID获取消息列表
     */
    @GetMapping("/api/hangzd/messages/sender/{senderId}")
    public Result<List<MessageDO>> getMessagesBySender(@PathVariable Long senderId) {
        return Results.success(messageService.getMessagesBySender(senderId));
    }

    /**
     * 根据发送者和接收者ID获取消息列表
     */
    @GetMapping("/api/hangzd/messages/sender/{senderId}/receiver/{receiverId}")
    public Result<List<MessageDO>> getMessagesBySenderAndReceiver(@PathVariable Long senderId, @PathVariable Long receiverId) {
        return Results.success(messageService.getMessagesBySenderAndReceiver(senderId, receiverId));
    }

    /**
     * 添加消息
     */
    @PostMapping("/api/hangzd/message")
    public Result<Void> addMessage(@RequestBody MessageUploadReqDTO message) {
        messageService.addMessage(message);
        return Results.success();
    }

    /**
     * 更新消息
     */
    @PutMapping("/api/hangzd/message")
    public Result<Void> updateMessage(@RequestBody MessageUpdateReqDTO message) {
        messageService.updateMessage(message);
        return Results.success();
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/api/hangzd/message/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return Results.success();
    }

    /**
     * 将本用户发送的未删除的消息按照创建时间降序排序
     */
    @GetMapping("/api/hangzd/messages/sort")
    public Result<List<MessageDO>> sortMessagesByCreateTime() {
        return Results.success(messageService.sortMessagesByCreateTime());
    }

    /**
     * 已读消息
     */
    @PutMapping("/api/hangzd/message/read/{id}")
    public Result<Void> readMessage(@PathVariable Long id) {
        messageService.readMessage(id);
        return Results.success();
    }

}
