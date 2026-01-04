package com.topicmodelling.mallet;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mallet")
// Train one Model per theme
public class MalletConfig {
    private String modelPath;
//    private String corpusPath;
    private String pipesPath;

    // getters and setters
    public String getModelPath() { return modelPath; }
    public void setModelPath(String modelPath) { this.modelPath = modelPath; }
//    public String getCorpusPath() { return corpusPath; }
//    public void setCorpusPath(String corpusPath) { this.corpusPath = corpusPath; }
    public String getPipesPath() { return pipesPath; }
    public void setPipesPath(String pipesPath) { this.pipesPath = pipesPath; }
}
