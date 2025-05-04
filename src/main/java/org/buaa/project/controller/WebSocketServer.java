package org.buaa.project.controller;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.buaa.project.common.convention.exception.ServiceException;
import org.springframework.stereotype.Component;
import static org.buaa.project.common.enums.MessageErrorCodeEnum.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;


@ServerEndpoint("/api/websocket/{userId}")
public class WebSocketServer {
    //private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    // 静态 Map 存放 userId 与 Session 的映射
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private String userId;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.userId = userId;
        sessions.put(userId, session);
        System.out.println("WebSocket connected: " + userId);
    }

    @OnClose
    public void onClose() {
        sessions.remove(userId);
        System.out.println("WebSocket disconnected: " + userId);
    }

    @OnError
    public void onError(Throwable t) {
        System.out.println("WebSocket error: " + userId + " - " + t.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Message from " + userId + ": " + message);
    }

    /**
     * 静态方法：向指定用户推送消息
     */
    public static void sendToUser(String toUserId, String msg) {
        Session session = sessions.get(toUserId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (Exception e) {
                throw new ServiceException(MESSAGE_SENT_FAILED);
            }
        }
    }
}
