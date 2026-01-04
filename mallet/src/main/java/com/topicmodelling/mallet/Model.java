package com.topicmodelling.mallet;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "themes")
public class Model {

    @Id
    private String modelID;
    private String modelURI;
}
