package com.topicmodelling.scraper;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Doc {

    private String articleId;
    private String title;
    private String rawContent;
    private String date;

    public Doc(String articleId, String title, String date, String rawContent) {
        this.articleId = articleId;
        this.title = title;
        this.rawContent = rawContent;
        this.date = date;
    }
}
