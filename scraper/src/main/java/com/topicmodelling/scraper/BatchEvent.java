package com.topicmodelling.scraper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchEvent {
    private String id;
    private Doc doc;
//    private UUID batchID;
    private boolean eob;
}
