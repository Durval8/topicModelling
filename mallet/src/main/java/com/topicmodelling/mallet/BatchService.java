package com.topicmodelling.mallet;

import com.mongodb.internal.connection.DualMessageSequences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class BatchService {

    @Autowired
    private BatchRepository batchRepository;
    private MongoTemplate mongoTemplate;

    public void saveEvent(BatchEvent batchEvent) {
        batchRepository.save(batchEvent);
    }

    public void saveEvents(List<BatchEvent> batchEvents) {
        batchRepository.saveAll(batchEvents);
    }

    public List<BatchEvent> getBatch(String modelID) {
        return batchRepository.findByDoc_ModelIDAndEobFalse(modelID);
    }

    public void cleanupBatch(String modelID) {
        long deleted = batchRepository.deleteByDoc_ModelID(modelID);
        System.out.println("DELETED " + deleted + " DOCS");
        if (deleted == 0) {
            System.out.println("No batch events found for " + modelID);
        }
    }
}
