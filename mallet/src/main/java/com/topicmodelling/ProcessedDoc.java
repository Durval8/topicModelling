package com.topicmodelling;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "processedDoc")
public class ProcessedDoc {

    @Id
    private String articleId;
    private String title;
    private String date;
    private String body;
    private double[] topics;
    private String theme;

    public ProcessedDoc(String articleId, String title, String date, String body, double[] topics, String theme) {
        this.articleId = articleId;
        this.title = title;
        this.date = date;
        this.body = body;
        this.topics = topics;
        this.theme = theme;
    }

}
