package com.topicmodelling.mallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class KafkaConsumerService {

    @Autowired
    private BatchService batchService;
    @Autowired
    private MalletService malletService;

    @KafkaListener(topics = "batch_doc", groupId = "mallet")
    public void listen(List<BatchEvent> batchEvents) {
        batchService.saveEvents(batchEvents);

        System.out.println("GOT " + batchEvents.size() + " DOCS");

        batchEvents.stream()
                .filter(BatchEvent::isEob)
                .forEach(e -> {
                    try {
                        System.out.println("GOT EOB");
                        malletService.trainModel(extractModelId(e));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    private String extractModelId(BatchEvent e) {
        return e.getId().split(":")[0];
    }
}
