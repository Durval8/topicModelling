package com.topicmodelling.docProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/docProcessor")
public class DocProcessorController {

    @Autowired
    private DocProcessorService docProcessorService;

    @GetMapping

    // IF NO MODEL IS FOUND TRIGGER MODEL TRAINED WITH THE DOCUMENTS SCRAPED RECEIVED FROM THE KAFKA LISTENER
    // IF TOO LITTLE DOCUMENTS, SEND A WARNING
}
