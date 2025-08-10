package com.assignmenthasan.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class ConversationSummary {
    private String otherUid;
    private String otherEmail;       // may be null if not synced yet
    private String lastTimestampIso; // ISO-8601 string
}
