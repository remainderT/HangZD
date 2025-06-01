package org.buaa.project.common.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("连接建立：" + session.getId());
        session.sendMessage(new TextMessage("连接成功"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String received = message.getPayload();
        System.out.println("收到消息: " + received);
        session.sendMessage(new TextMessage("你发送了: " + received));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("连接关闭：" + session.getId());
    }
}
