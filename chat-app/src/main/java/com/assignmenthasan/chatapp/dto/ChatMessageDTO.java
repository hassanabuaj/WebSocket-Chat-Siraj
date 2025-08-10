package com.assignmenthasan.chatapp.dto;

import lombok.Data;

/**
 * אובייקט העברת נתונים להודעת צ'אט
 * משמש להעברת הודעות בין הלקוח לשרת דרך WebSocket
 */
@Data
public class ChatMessageDTO {
    /** מזהה המשתמש ששולח את ההודעה */
    private String senderId;
    
    /** מזהה המשתמש שאמור לקבל את ההודעה */
    private String receiverId;
    
    /** חותמת זמן בפורמט ISO (הלקוח יכול לשלוח או שהשרת יקבע) */
    private String timestamp;
    
    /** תוכן ההודעה */
    private String message;
}
