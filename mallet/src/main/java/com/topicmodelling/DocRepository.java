package com.topicmodelling;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository("rawDocuments")
public interface DocRepository extends MongoRepository<Doc, String> { }
