package com.topicmodelling.mallet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndex(def = "{ 'doc.modelID': 1 }")
@Document(collection = "batches")
public class BatchEvent {

    @Id
    private String id;

//    private UUID batchID;
    private Doc doc;
    private boolean eob;
}
