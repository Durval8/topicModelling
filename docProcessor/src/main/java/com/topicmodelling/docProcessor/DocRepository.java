package com.topicmodelling.docProcessor;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository("raw_docs")
public interface DocRepository extends MongoRepository<Doc, String> {
}
