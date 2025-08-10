package com.assignmenthasan.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * מחלקת התחלה ראשית של אפליקציית הצ'אט
 * משתמשת ב-WebSocket לתקשורת בזמן אמת, MongoDB לאחסון הודעות ו-Firebase לאימות
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ChatAppApplication {
	/**
	 * נקודת הכניסה הראשית של האפליקציה
	 * @param args ארגומנטים מהשורת פקודה
	 */
	public static void main(String[] args) {
		SpringApplication.run(ChatAppApplication.class, args);
	}
}
