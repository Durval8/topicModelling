package com.topicmodelling.scraper;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "raw_docs")
public class Doc {

    @Id
    String articleId;   // unique per article
    String title;
    String date;        // ISO-8601 yyyy-MM-dd
    String rawContent;

    public Doc(String articleId, String title, String date, String rawContent) {
        this.articleId = articleId;
        this.title = title;
        this.rawContent = rawContent;
        this.date = date;
    }
}
