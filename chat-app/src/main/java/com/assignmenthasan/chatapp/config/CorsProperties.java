package com.assignmenthasan.chatapp.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * מחלקת תצורה להגדרות CORS
 * מאפשרת הגדרה גמישה של מקורות מורשים באמצעות קובץ תצורה
 */
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * מקורות מדויקים לאישור (למשל http://localhost:4200). אופציונלי.
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * תבניות מקורות לאישור (למשל https://*.web.app). מומלץ עבור Firebase Hosting.
     */
    private List<String> allowedOriginPatterns = new ArrayList<>(List.of(
            "https://*.web.app",
            "https://*.firebaseapp.com",
            "http://localhost:4200"
    ));

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }
}


