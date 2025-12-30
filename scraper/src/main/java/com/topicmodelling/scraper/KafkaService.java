package com.topicmodelling.scraper;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
    private static final String TOPIC = "Raw_Docs";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Doc doc) {
        kafkaTemplate.send("raw_doc", doc);
        System.out.println("Sent doc: " + doc.getTitle());
    }
}
