package com.topicmodelling.docProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocProcessorService {

    @Autowired
    private DocRepository docRepository;

    private final MongoTemplate mongoTemplate;

    public DocProcessorService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void saveDocs(List<Doc> docs) {
        docRepository.saveAll(docs);
    }

    public List<Doc> getDocsByTheme(String theme) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("theme").is(theme))
        );

        return mongoTemplate
                .aggregate(agg, "raw_docs", Doc.class)
                .getMappedResults();
    }

    public List<Doc> generateCorpus(String theme) {
        long themeCount = mongoTemplate.count(
                Query.query(Criteria.where("theme").is(theme)),
                "raw_docs"
        );

        if (themeCount == 0) {
            return List.of();
        }

        int sampleSize = (int) Math.min(themeCount, 500);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("theme").is(theme)),
                Aggregation.sample(sampleSize)
        );

        return mongoTemplate
                .aggregate(agg, "raw_docs", Doc.class)
                .getMappedResults();
    }
}
