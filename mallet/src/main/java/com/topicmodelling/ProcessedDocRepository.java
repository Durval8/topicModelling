package com.topicmodelling;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository("processedDoc")
public interface ProcessedDocRepository extends MongoRepository<ProcessedDoc, String> { }