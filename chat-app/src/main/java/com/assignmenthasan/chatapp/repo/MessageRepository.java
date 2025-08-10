package com.assignmenthasan.chatapp.repo;

import com.assignmenthasan.chatapp.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findTop50BySenderIdAndReceiverIdOrderByTimestampDesc(String sender, String receiver);
    List<Message> findTop50BySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampDesc(
            String s1, String r1, String s2, String r2);
}
