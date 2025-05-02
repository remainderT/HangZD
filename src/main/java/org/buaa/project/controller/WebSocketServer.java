package org.buaa.project.controller;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@ServerEndpoint(value="/api/websocket/{user_id}")
public class WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class); // Logger声明
    private static final Map<Long, Session> sessions = new ConcurrentHashMap<Long, Session>();
    private Long userId;

    @OnOpen
    public void onOpen(Session session, @PathParam("user_id") Long userId) {
        this.userId = userId;
        sessions.put(userId,session);
        logger.info("Websocket connected: {}", userId);
    }

    @OnClose
    public void onClose() {
        sessions.remove(userId);
        logger.info("Websocket disconnected: {}", userId);
    }

    @OnError
    public void onError(Throwable error) {
        sessions.remove(userId);
        logger.error("Websocket error: {}", error.getMessage());
    }
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            logger.info("Received message from {}: {}", userId, message);
            //session.getBasicRemote().sendText("RECEIVED " + message);
        } catch (Exception e) {
            logger.error("Error handling message", e);
        }
    }

    public static void sendToUser(Long toUserId, String msg) {
        Session session = sessions.get(toUserId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (Exception e) {
                logger.error("Push to {} failed", toUserId, e);
            }
        }
    }
}
