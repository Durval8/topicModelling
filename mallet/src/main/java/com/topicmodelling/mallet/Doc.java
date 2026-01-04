package com.topicmodelling.mallet;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "raw_docs")
public class Doc {

    @Id
    private String articleId;   // unique per article
    private String title;
    private String date;        // ISO-8601 yyyy-MM-dd
    private String rawContent;
    private String modelID;
}