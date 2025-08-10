package com.assignmenthasan.chatapp.config;

import java.io.FileInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

/**
 * תצורה לאתחול Firebase Admin SDK
 * מאתחלת את החיבור ל-Firebase באמצעות קובץ חשבון שירות
 */
@Configuration
public class FirebaseConfig {

    @Value("${app.firebase.serviceAccount}")
    private String serviceAccountPath;

    /**
     * מאתחלת את Firebase App עם אישורי חשבון השירות
     * נקראת אוטומטית לאחר יצירת הבין
     * @throws Exception אם יש בעיה בטעינת האישורים או באתחול Firebase
     */
    @PostConstruct
    public void init() throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            try (FileInputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }
        }
    }
}
