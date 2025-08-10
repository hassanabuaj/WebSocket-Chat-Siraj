package com.assignmenthasan.chatapp.ws;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

/**
 * מחלקה לאימות חיבורי WebSocket באמצעות Firebase
 * בודקת את תוקן Firebase בבקשת החיבור ומוסיפה את מזהה המשתמש לתכונות החיבור
 */
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * נקראת לפני יצירת חיבור WebSocket חדש
     * מאמתת את תוקן Firebase ומוסיפה את מזהה המשתמש לתכונות החיבור
     * @param request הבקשה הנכנסת
     * @param response התגובה
     * @param wsHandler מטפל ה-WebSocket
     * @param attributes מפת תכונות החיבור
     * @return true אם האימות הצליח, false אחרת
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletReq)) {
            return false;
        }
        String token = servletReq.getServletRequest().getParameter("token");
        if (token == null || token.isBlank()) return false;

        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
            attributes.put("uid", decoded.getUid());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * נקראת לאחר השלמת לחיצת היד של WebSocket
     * אין צורך בפעולות נוספות במימוש זה
     * @param request הבקשה
     * @param response התגובה
     * @param wsHandler מטפל ה-WebSocket
     * @param exception חריגה אם אירעה
     */
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }
}
