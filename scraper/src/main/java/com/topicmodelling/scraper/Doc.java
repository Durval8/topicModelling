package com.topicmodelling.scraper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
//@Document(collection = "raw_docs")
public class Doc {

    @Id
    private String articleId;   // unique per article
    private String title;
    private String date;        // ISO-8601 yyyy-MM-dd
    private String rawContent;
    private String theme;

//    public Doc(String articleId, String title, String date, String rawContent, String theme) {
//        this.articleId = articleId;
//        this.title = title;
//        this.rawContent = rawContent;
//        this.date = date;
//        this.theme = theme;
//    }
}
