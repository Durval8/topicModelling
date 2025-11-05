package com.topicmodelling;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository("topics")
public interface TopicRepository extends MongoRepository<Topic, String> { }