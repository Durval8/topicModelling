package com.topicmodelling.scraper;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaService {
    private static final String TOPIC = "Raw_Docs";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void process(Doc doc) {
        kafkaTemplate.send("raw_doc", doc);
        System.out.println("Sent doc: " + doc.getTitle());
    }

    public void batch(Doc doc, String batchId, boolean eob) {
        String s;
        if (eob) {
            s = "EOB";
        } else {
            if (doc == null) {
                throw new IllegalArgumentException("doc must not be null for DOC event");
            }
            s = doc.getTitle();
        }
        kafkaTemplate.send(
                "batch_doc",
                batchId,
                new BatchEvent(
                        batchId + ":" + s,
                        doc,
//                        batchId,
                        eob             // end of batch
                )
        );
    }
}
