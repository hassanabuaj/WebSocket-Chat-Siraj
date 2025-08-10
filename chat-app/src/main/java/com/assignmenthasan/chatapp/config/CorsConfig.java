package com.assignmenthasan.chatapp.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
/**
 * הגדרת CORS עבור האפליקציה.
 * מאפשר לקבוע אילו מקורות (Origins), שיטות (Methods) וכותרות (Headers) מותרות
 * בזמן תקשורת בין הדפדפן לשרת ממקורות שונים.
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {
    /**
     * יוצר ומחזיר Bean אובייקט מסוג CorsConfigurationSource,
     * אשר Spring ישתמש בו כדי להחיל את הגדרות ה-CORS.
     *
     * @param props אובייקט המכיל את הגדרות ה־CORS (נשלף מתוך application.yml/properties)
     * @return מקור קונפיגורציית CORS שמיושם על כל הנתיבים
     */

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        // אם יש מקורות מותרים, נוסיף אותם
        if (!props.getAllowedOrigins().isEmpty()) {
            config.setAllowedOrigins(props.getAllowedOrigins());
        }
        // אם יש תבניות מקורות מותרים (Patterns), נוסיף גם אותם
        if (!props.getAllowedOriginPatterns().isEmpty()) {
            config.setAllowedOriginPatterns(props.getAllowedOriginPatterns());
        }
        // שיטות HTTP שמותרות
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // כותרות שמותרות בבקשות
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "ngrok-skip-browser-warning"
        ));
        // כותרות שייחשפו בתגובה
        config.setExposedHeaders(List.of("Content-Type"));
        // לא מאפשר Credentials (Cookies / Session)
        config.setAllowCredentials(false); // Bearer tokens, not cookies
        // רישום ההגדרות על כל הנתיבים בשרת
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
