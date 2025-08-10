package com.assignmenthasan.chatapp.controller;

import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.assignmenthasan.chatapp.dto.UserLookupResponse;
import com.assignmenthasan.chatapp.model.AppUser;
import com.assignmenthasan.chatapp.repo.AppUserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

/**
 * בקר REST לניהול משתמשים
 * מספק נקודות קצה לסנכרון משתמשים וחיפוש לפי דוא"ל או מזהה
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserRepository repo;

    /**
     * בנאי המקבל את מאגר המשתמשים
     * @param repo מאגר משתמשים למסד הנתונים
     */
    public UserController(AppUserRepository repo) {
        this.repo = repo;
    }

    /**
     * מסנכרן את פרטי המשתמש הנוכחי למסד הנתונים
     * נקרא על ידי הלקוח לאחר התחברות להוסיף/לעדכן את פרטי המשתמש
     * @param authHeader כותרת האימות עם תוקן Firebase
     * @return פרטי המשתמש שנשמרו או שגיאה
     */
    @PutMapping("/me")
    public ResponseEntity<?> upsertMe(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            String token = stripBearer(authHeader);
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);

            String uid = decoded.getUid();
            String email = decoded.getEmail();           // may be null if provider didn’t supply; fine
            String name = (String) decoded.getClaims().getOrDefault("name", decoded.getName());

            var existing = repo.findById(uid).orElse(null);
            AppUser user = (existing == null)
                    ? AppUser.builder().uid(uid).email(email).displayName(name).updatedAt(Instant.now()).build()
                    : AppUser.builder()
                    .uid(uid)
                    .email(email != null ? email : existing.getEmail())
                    .displayName(name != null ? name : existing.getDisplayName())
                    .updatedAt(Instant.now())
                    .build();

            repo.save(user);
            return ResponseEntity.ok(new UserLookupResponse(user.getUid(), user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"Unable to sync user\"}");
        }
    }

    /**
     * מחפש משתמש לפי דוא"ל או מזהה ייחודי
     * מונע חיפוש של המשתמש עצמו למניעת שליחת הודעות לעצמו
     * @param auth פרטי האימות של המשתמש הנוכחי
     * @param email כתובת דוא"ל לחיפוש (אופציונלי)
     * @param uid מזהה ייחודי לחיפוש (אופציונלי)
     * @return פרטי המשתמש שנמצא או שגיאה
     */
    @GetMapping("/resolve")
    public ResponseEntity<?> resolve(Authentication auth,
                                     @RequestParam(required = false) String email,
                                     @RequestParam(required = false) String uid) {
        String currentUserId = (String) auth.getPrincipal();
        
        if (email != null && !email.isBlank()) {
            return repo.findByEmailIgnoreCase(email.trim())
                    .<ResponseEntity<?>>map(u -> {
                        if (u.getUid().equals(currentUserId)) {
                            return ResponseEntity.status(400).body("{\"error\":\"Cannot start conversation with yourself\"}");
                        }
                        return ResponseEntity.ok(new UserLookupResponse(u.getUid(), u.getEmail()));
                    })
                    .orElseGet(() -> ResponseEntity.status(404).body("{\"error\":\"User not found\"}"));
        } else if (uid != null && !uid.isBlank()) {
            if (uid.trim().equals(currentUserId)) {
                return ResponseEntity.status(400).body("{\"error\":\"Cannot start conversation with yourself\"}");
            }
            return repo.findById(uid.trim())
                    .<ResponseEntity<?>>map(u -> ResponseEntity.ok(new UserLookupResponse(u.getUid(), u.getEmail())))
                    .orElseGet(() -> ResponseEntity.status(404).body("{\"error\":\"User not found\"}"));
        } else {
            return ResponseEntity.badRequest().body("{\"error\":\"Provide email or uid\"}");
        }
    }

    /**
     * מסירה את הקידומת "Bearer " מכותרת האימות
     * @param header כותרת האימות
     * @return התוקן ללא הקידומת או null אם הכותרת ריקה
     */
    private static String stripBearer(String header) {
        if (header == null) return null;
        return header.startsWith("Bearer ") ? header.substring(7) : header;
    }
}
