package com.topicmodelling.mallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModelService {

    @Autowired
    private ModelRepository modelRepository;

    public boolean exists(String theme) {
        return modelRepository.existsById(theme);
    }

    public void newModel(Model model) {
        modelRepository.insert(model);
    }
}
