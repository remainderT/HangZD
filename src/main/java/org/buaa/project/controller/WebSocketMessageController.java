package org.buaa.project.controller;

import lombok.RequiredArgsConstructor;
import org.buaa.project.dto.resp.MessageRespDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final SimpMessagingTemplate messagingTemplate;

    // 广播消息
    @MessageMapping("/broadcast")
    @SendTo("/topic/messages")
    public MessageRespDTO broadcastMessage(MessageRespDTO message) {
        return message;
    }

    // 点对点消息
    @MessageMapping("/private")
    public void privateMessage(MessageRespDTO message) {
        messagingTemplate.convertAndSendToUser(String.valueOf(message.getToId()), "/queue/messages", message);
    }
}