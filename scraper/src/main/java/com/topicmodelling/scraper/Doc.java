package com.topicmodelling.scraper;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Doc {
    String articleId;   // unique per article
    String title;
    String date;        // ISO-8601 yyyy-MM-dd
    String rawContent;
}
