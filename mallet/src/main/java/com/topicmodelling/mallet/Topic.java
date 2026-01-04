package com.topicmodelling.mallet;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "topics")
public class Topic {

    @Id
    String topicName;
    List<String> topWords;

    public Topic(String topicName, List<String> topWords) {
        this.topicName = topicName;
        this.topWords = topWords;
    }
}
