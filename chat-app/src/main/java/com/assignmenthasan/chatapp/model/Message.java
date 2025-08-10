package com.assignmenthasan.chatapp.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * מודל הודעת צ'אט במסד הנתונים
 * מכיל את כל הפרטים הנדרשים להודעה לפי דרישות המטלה
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "messages")
public class Message {
    /** מזהה ייחודי של ההודעה */
    @Id
    private String id;

    /** מזהה המשתמש ששלח את ההודעה */
    private String senderId;
    
    /** מזהה המשתמש שאמור לקבל את ההודעה */
    private String receiverId;
    
    /** חותמת זמן של יצירת ההודעה (נשמרת כ-Instant, מוצגת כ-ISO) */
    private Instant timestamp;
    
    /** תוכן ההודעה */
    private String message;
}
