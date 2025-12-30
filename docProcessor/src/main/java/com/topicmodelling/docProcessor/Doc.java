package com.topicmodelling.docProcessor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doc {

    @Id
    private String articleId;   // unique per article
    private String title;
    private String date;        // ISO-8601 yyyy-MM-dd
    private String rawContent;
    private String theme;
}