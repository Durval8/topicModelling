package com.topicmodelling.scraper;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository("documents")
public interface DocRepository extends MongoRepository<Doc, String> {

}
