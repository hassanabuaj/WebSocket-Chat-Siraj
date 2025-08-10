package com.assignmenthasan.chatapp.controller;

import com.assignmenthasan.chatapp.dto.ConversationSummary;
import com.assignmenthasan.chatapp.model.AppUser;
import com.assignmenthasan.chatapp.repo.AppUserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Controller לשיחות – מחזיר סיכומי שיחות אחרונות למשתמש המחובר.
 * נתיב בסיס: /api/conversations
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final MongoTemplate mongoTemplate;
    private final AppUserRepository userRepo;

    public ConversationController(MongoTemplate mongoTemplate, AppUserRepository userRepo) {
        this.mongoTemplate = mongoTemplate;
        this.userRepo = userRepo;
    }

    /**
     * GET /api/conversations/recent?limit=20
     *
     * מאמת את המשתמש באמצעות Firebase ID Token, ואז מחזיר רשימת סיכומי שיחות
     * (otherUid, otherEmail, lastTimestampIso) ממוינת לפי זמן הודעה אחרונה.
     *
     * @param authHeader כותרת Authorization בפורמט "Bearer <token>"
     * @param limit      כמות תוצאות מבוקשת (ברירת מחדל 20)
     * @return 200 OK עם רשימת ConversationSummary, או 400 במקרה כשל
     */
    // GET /api/conversations/recent?limit=20
    @GetMapping("/recent")
    public ResponseEntity<?> recent(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            // חילוץ הטוקן מה־Authorization header
            String token = stripBearer(authHeader);

            // אימות הטוקן וקבלת נתוני המשתמש מ־Firebase
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
            String me = decoded.getUid();

            // שלב 1: סינון רק להודעות שבהן אני שולח או מקבל
            MatchOperation matchMine = match(new Criteria().orOperator(
                    Criteria.where("senderId").is(me),
                    Criteria.where("receiverId").is(me)
            ));

            // שלב 2: otherId = אם אני השולח → הנמען; אחרת → השולח
            var otherIdExpr = ConditionalOperators.when(Criteria.where("senderId").is(me))
                    .thenValueOf("receiverId")
                    .otherwiseValueOf("senderId");

            // נשמר גם את timestamp לצורך שלב ה־group (max)
            ProjectionOperation projectOther = project("senderId", "receiverId", "timestamp")
                    .and(otherIdExpr).as("otherId");

            // שלב 3: קיבוץ לפי otherId ובחירת הזמן האחרון
            GroupOperation groupByOther = group("otherId")
                    .max("timestamp").as("lastTs");

            // שלב 4+5: מיון יורד לפי הזמן האחרון והגבלת כמות
            SortOperation sortByLast = sort(Sort.Direction.DESC, "lastTs");
            LimitOperation limitOp = limit(limit);

            Aggregation agg = newAggregation(
                    matchMine,
                    projectOther,
                    groupByOther,
                    sortByLast,
                    limitOp
            );

            AggregationResults<Document> res =
                    mongoTemplate.aggregate(agg, "messages", Document.class);
            List<Document> docs = res.getMappedResults();

            // Fetch emails for the "otherId" values
            List<String> otherIds = docs.stream()
                    .map(d -> d.getString("_id"))
                    .collect(Collectors.toList());

            Map<String, String> emailByUid = userRepo.findAllById(otherIds).stream()
                    .collect(Collectors.toMap(AppUser::getUid, AppUser::getEmail, (a, b) -> a));

            List<ConversationSummary> out = new ArrayList<>();
            for (Document d : docs) {
                String otherUid = d.getString("_id");
                Date lastTs = d.getDate("lastTs");
                out.add(new ConversationSummary(
                        otherUid,
                        emailByUid.get(otherUid),
                        (lastTs != null ? Instant.ofEpochMilli(lastTs.getTime()).toString() : null)
                ));
            }

            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("{\"error\":\"unable to load recent\"}");
        }
    }

    private static String stripBearer(String header) {
        if (header == null) return null;
        return header.startsWith("Bearer ") ? header.substring(7) : header;
    }
}
