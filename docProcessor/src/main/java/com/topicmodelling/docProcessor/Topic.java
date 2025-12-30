package com.topicmodelling.docProcessor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Topic {

    @Id
    String topicName;
    List<String> topWords;
}
