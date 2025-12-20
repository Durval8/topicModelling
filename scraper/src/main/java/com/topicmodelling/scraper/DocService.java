package com.topicmodelling.scraper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocService {

    @Autowired
    private DocRepository docRepository;

    public void addDoc(Doc doc) {
        docRepository.save(doc);
    }

    public  boolean containsDoc(Doc doc) {
        return docRepository.findById(doc.getArticleId()).isPresent();
    }
}
