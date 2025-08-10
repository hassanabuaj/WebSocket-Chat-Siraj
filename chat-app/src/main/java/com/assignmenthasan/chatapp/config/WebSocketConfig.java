package com.assignmenthasan.chatapp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.assignmenthasan.chatapp.ws.AuthHandshakeInterceptor;
import com.assignmenthasan.chatapp.ws.ChatWebSocketHandler;

/**
 * תצורת WebSocket לאפליקציית הצ'אט
 * מגדירה את נקודות הקצה של WebSocket ואת הגדרות CORS המתאימות
 */
@Configuration
@EnableWebSocket
@EnableConfigurationProperties(CorsProperties.class)
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final AuthHandshakeInterceptor authInterceptor;
    private final CorsProperties corsProperties;

    /**
     * בנאי המקבל את כל הרכיבים הנדרשים
     * @param chatWebSocketHandler מטפל הודעות WebSocket
     * @param authInterceptor מיירט אימות לחיבורי WebSocket
     * @param corsProperties הגדרות CORS
     */
    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler,
                           AuthHandshakeInterceptor authInterceptor,
                           CorsProperties corsProperties) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.authInterceptor = authInterceptor;
        this.corsProperties = corsProperties;
    }

    /**
     * רושמת את מטפלי WebSocket עם הגדרות CORS ואימות
     * @param registry רישום מטפלי WebSocket
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Note: Spring WebSocket supports setAllowedOrigins (exact) and setAllowedOriginPatterns (patterns in Spring 5.3+)
        var reg = registry.addHandler((WebSocketHandler) chatWebSocketHandler, "/ws/chat")
                .addInterceptors(authInterceptor);

        if (!corsProperties.getAllowedOrigins().isEmpty()) {
            reg.setAllowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new));
        }
        if (!corsProperties.getAllowedOriginPatterns().isEmpty()) {
            reg.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns().toArray(String[]::new));
        }
        if (corsProperties.getAllowedOrigins().isEmpty() && corsProperties.getAllowedOriginPatterns().isEmpty()) {
            reg.setAllowedOrigins("*");
        }
    }
}
