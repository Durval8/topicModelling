package com.topicmodelling.scraper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocService {

    @Autowired
    private DocRepository docRepository;

    public void addDocument(Doc document) {
        docRepository.save(document);
    }

    public boolean containsDocument(Doc document) { return docRepository.findById(document.getArticleId()).isPresent(); }

}
