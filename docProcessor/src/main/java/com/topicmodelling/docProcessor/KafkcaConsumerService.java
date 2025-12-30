package com.topicmodelling.docProcessor;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkcaConsumerService {

    private static final String TOPIC = "Raw_Docs";

    @KafkaListener(topics = "raw_doc", groupId = "docProcessor")
    public void listen(Doc doc) {

        System.out.println("Received doc " + doc.getTitle());
    }
}
