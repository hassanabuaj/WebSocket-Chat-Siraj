package com.assignmenthasan.chatapp.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * מודל משתמש אפליקציה במסד הנתונים
 * מאחסן פרטי משתמשים לצורך חיפוש וזיהוי במערכת הצ'אט
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "users")
public class AppUser {
    /** מזהה ייחודי של המשתמש מ-Firebase */
    @Id
    private String uid;

    /** כתובת דוא"ל של המשתמש (ייחודית, אינדקס דליל) */
    @Indexed(unique = true, sparse = true)
    private String email;

    /** שם התצוגה של המשתמש */
    private String displayName;

    /** זמן העדכון האחרון של פרטי המשתמש */
    private Instant updatedAt;
}
