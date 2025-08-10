package com.assignmenthasan.chatapp.service;

import java.net.URI;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.assignmenthasan.chatapp.dto.ChatMessageDTO;
import com.assignmenthasan.chatapp.model.Message;
import com.assignmenthasan.chatapp.repo.MessageRepository;

/**
 * שירות לניהול הודעות צ'אט
 * אחראי על שמירת הודעות במסד הנתונים והפעלת פונקציית Firebase להתראות
 */
@Service
public class ChatService {

    private final MessageRepository repo;
    private final RestTemplate rest;

    @Value("${app.functions.notifyUrl}")
    private String notifyUrl;

    /**
     * בנאי המקבל את מאגר ההודעות
     * @param repo מאגר הודעות למסד הנתונים
     */
    public ChatService(MessageRepository repo) {
        this.repo = repo;
        this.rest = new RestTemplate();
    }

    /**
     * שומרת הודעה חדשה במסד הנתונים
     * מוסיפה חותמת זמן שרת ושומרת את ההודעה
     * @param dto אובייקט העברת נתונים של ההודעה
     * @return ההודעה השמורה עם מזהה ייחודי
     */
    public Message persist(ChatMessageDTO dto) {
        Message msg = Message.builder()
                .senderId(dto.getSenderId())
                .receiverId(dto.getReceiverId())
                .timestamp(Instant.now())
                .message(dto.getMessage())
                .build();
        return repo.save(msg);
    }

    /**
     * מפעילה פונקציית Firebase להתראה על הודעה חדשה
     * שולחת בקשת HTTP לפונקציית Firebase עם פרטי ההודעה
     * @param msg ההודעה שנשלחה
     */
    public void notifyFunction(Message msg) {
        try {
            var payload = """
                {"receiverId":"%s","senderId":"%s","messageId":"%s"}
                """.formatted(msg.getReceiverId(), msg.getSenderId(), msg.getId());

            RequestEntity<String> req = RequestEntity
                    .post(URI.create(notifyUrl))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload);
            rest.exchange(req, String.class);
        } catch (Exception ignored) {
            // non-critical: assignment allows console log in the function.
        }
    }
}
