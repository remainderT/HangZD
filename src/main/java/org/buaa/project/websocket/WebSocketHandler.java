package org.buaa.project.websocket;

import com.alibaba.fastjson2.JSON;
import org.buaa.project.dao.entity.MessageDO;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            userSessions.put(userId, session);
            System.out.println("用户上线: " + userId);
        }
        session.sendMessage(new TextMessage("连接成功"));
    }

    public void send(MessageDO message) throws Exception {
        Long toId = message.getToId();
        WebSocketSession toSession = userSessions.get(toId);

        if (toSession != null && toSession.isOpen()) {
            toSession.sendMessage(new TextMessage(JSON.toJSONString(message)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            userSessions.remove(userId);
            System.out.println("用户下线: " + userId);
        }
    }

    private Long getUserId(WebSocketSession session) {
        String uri = session.getUri().toString();
        if (uri.contains("userId=")) {
            return Long.parseLong(uri.substring(uri.indexOf("userId=") + 7));
        }
        return null;
    }

}
