package com.topicmodelling.mallet;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface BatchRepository extends MongoRepository<BatchEvent, String> {
    List<BatchEvent> findByDoc_ModelIDAndEobFalse(String modelID);

    long deleteByDoc_ModelID(String modelID);
}
