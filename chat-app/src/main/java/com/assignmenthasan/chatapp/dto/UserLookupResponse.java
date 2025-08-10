package com.assignmenthasan.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserLookupResponse {
    private String uid;
    private String email;
}
