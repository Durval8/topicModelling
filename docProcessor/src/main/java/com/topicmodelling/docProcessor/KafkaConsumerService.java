package com.topicmodelling.docProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaConsumerService {

    private static final String TOPIC = "Raw_Docs";

    @Autowired
    private DocProcessorService docProcessorService;

    @KafkaListener(topics = "raw_doc", groupId = "docProcessor")
    public void listen(List<Doc> docs) {
        for (Doc doc: docs) {
            System.out.println("Received doc " + doc.getTitle());
        }
        docProcessorService.saveDocs(docs);
    }
}
