package com.assignmenthasan.chatapp.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.assignmenthasan.chatapp.model.Message;
import com.assignmenthasan.chatapp.repo.MessageRepository;

/**
 * בקר REST לניהול הודעות צ'אט
 * מספק נקודות קצה לשליפת היסטוריית הודעות
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository repo;

    /**
     * בנאי המקבל את מאגר ההודעות
     * @param repo מאגר הודעות למסד הנתונים
     */
    public MessageController(MessageRepository repo) {
        this.repo = repo;
    }

    /**
     * מחזירה הודעות אחרונות עם משתמש מסוים
     * @param auth פרטי האימות של המשתמש הנוכחי
     * @param withUser מזהה המשתמש השני בשיחה
     * @param limit מספר ההודעות המקסימלי להחזיר (ברירת מחדל: 50)
     * @return רשימת הודעות ממוינות לפי זמן
     */
    @GetMapping
    public ResponseEntity<List<Message>> getRecent(
            Authentication auth,
            @RequestParam String withUser,
            @RequestParam(defaultValue = "50") int limit) {

        String me = (String) auth.getPrincipal();
        var list = repo.findTop50BySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampDesc(
                me, withUser, withUser, me);
        // repo method is desc; sort ascending for UI if desired:
        list.sort(Comparator.comparing(Message::getTimestamp));
        if (limit < list.size()) list = list.subList(list.size() - limit, list.size());
        return ResponseEntity.ok(list);
    }
}
