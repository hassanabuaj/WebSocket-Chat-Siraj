package com.assignmenthasan.chatapp.config;

import com.assignmenthasan.chatapp.security.FirebaseAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    /**
     * מגדיר את שרשרת מסנני האבטחה (Security Filter Chain).
     *
     * @param http אובייקט התצורה הראשי של Spring Security ל־HTTP
     * @return SecurityFilterChain שנבנה לפי ההגדרות למטה
     * @throws Exception אם יש כשל בבניית התצורה
     */

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ב־REST APIs מבוססי טוקנים אין צורך ב־CSRF
                .csrf(csrf -> csrf.disable())

                // הפעלת CORS; ההגדרות עצמן מגיעות מ־CorsConfigurationSource (ראה CorsConfig)
                .cors(cors -> {}) // allow defaults; configure if you need

                // מצב Stateless: אין Session בצד השרת. כל בקשה חייבת טוקן תקין
                .sessionManagement(sm -> sm.sessionCreationPolicy(
                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS))

                // הרשאות גישה לפי מסלולים
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()  // בריאות המערכת פתוח
                        .requestMatchers("/ws/**").permitAll()          // אימות WS מתבצע בזמן handshake
                        .anyRequest().authenticated()                   // כל השאר דורש אימות
                )
                // ביטול מנגנוני התחברות מבוססי דפדפן
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable());

        // הוספת מסנן האימות של Firebase לפני מסנן שם-משתמש/סיסמה
        http.addFilterBefore(new FirebaseAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // בניית השרשרת
        return http.build();
    }
}
