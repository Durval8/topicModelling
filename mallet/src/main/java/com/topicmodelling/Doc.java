package com.topicmodelling;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "documents")
public class Doc {

    @Id
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
