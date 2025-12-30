package com.topicmodelling.docProcessor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedDoc {

    @Id
    private String articleId;
    private String title;
    private String data;
    private String content;
    private List<Topic> topics;
    private String theme;
}
