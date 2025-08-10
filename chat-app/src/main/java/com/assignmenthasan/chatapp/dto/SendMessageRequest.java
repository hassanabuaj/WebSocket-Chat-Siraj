package com.assignmenthasan.chatapp.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String receiverId;
    private String message;
}
