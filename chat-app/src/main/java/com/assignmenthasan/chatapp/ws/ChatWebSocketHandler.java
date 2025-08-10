package com.assignmenthasan.chatapp.ws;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.assignmenthasan.chatapp.dto.ChatMessageDTO;
import com.assignmenthasan.chatapp.model.Message;
import com.assignmenthasan.chatapp.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * מחלקה לטיפול בחיבורי WebSocket עבור הצ'אט
 * מנהלת את החיבורים הפעילים ומעבירה הודעות בין המשתמשים בזמן אמת
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatService chatService;

    /** מפה המשמרת את החיבורים הפעילים - מזהה משתמש לחיבור */
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * בנאי המקבל את שירות הצ'אט
     * @param chatService שירות לשמירת הודעות והפעלת פונקציית Firebase
     */
    public ChatWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * נקראת כאשר חיבור WebSocket חדש נוצר בהצלחה
     * מוסיפה את החיבור למפת החיבורים הפעילים
     * @param session החיבור החדש שנוצר
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String uid = (String) session.getAttributes().get("uid");
        if (uid != null) {
            sessions.put(uid, session);
        }
    }

    /**
     * מטפלת בהודעות טקסט הנשלחות דרך WebSocket
     * מאמתת את זהות השולח, שומרת את ההודעה ומעבירה אותה לנמען
     * @param session החיבור שממנו נשלחה ההודעה
     * @param message ההודעה שנשלחה
     * @throws IOException במקרה של שגיאה בקריאה או כתיבה
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String uid = (String) session.getAttributes().get("uid");
        if (uid == null) {
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthenticated"));
            } catch (Exception ignored) {}
            return;
        }

        ChatMessageDTO dto = objectMapper.readValue(message.getPayload(), ChatMessageDTO.class);

        if (!uid.equals(dto.getSenderId())) {
            session.sendMessage(new TextMessage("{\"error\":\"senderId mismatch\"}"));
            return;
        }

        if (dto.getSenderId().equals(dto.getReceiverId())) {
            session.sendMessage(new TextMessage("{\"error\":\"cannot send message to yourself\"}"));
            return;
        }

        Message saved = chatService.persist(dto);

        WebSocketSession recipient = sessions.get(dto.getReceiverId());
        if (recipient != null && recipient.isOpen()) {
            recipient.sendMessage(new TextMessage(objectMapper.writeValueAsString(dto)));
        }

        if (session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(dto)));
        }

        chatService.notifyFunction(saved);
    }

    /**
     * נקראת כאשר חיבור WebSocket נסגר
     * מסירה את החיבור ממפת החיבורים הפעילים
     * @param session החיבור שנסגר
     * @param status סטטוס הסגירה
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String uid = (String) session.getAttributes().get("uid");
        if (uid != null) sessions.remove(uid);
    }
}
