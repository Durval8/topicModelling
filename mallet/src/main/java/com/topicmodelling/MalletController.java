package com.topicmodelling;

import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "/api/mallet", produces = "application/json")
public class MalletController {

    @Autowired
    private MalletService malletService;

    @GetMapping("/train")
    public void trainTopicModels() throws IOException {
        processDocuments();

        InputStream dataInputStream = new FileInputStream(malletService.getTrainDataPath().toString());
        InputStream stoplistInputStream = MalletController.class.getResourceAsStream("/stoplist_en.txt");
        SerialPipes pipeList = createPipes(stoplistInputStream);

        malletService.savePipes(pipeList);

        InstanceList instances = createInstanceList(dataInputStream, pipeList);

        int numTopics = 40;
        int numIterations = 300;
        int numTopWords = 25;

        ParallelTopicModel topicModel = trainTopicModel(instances, numTopics, numIterations, numTopWords);
        malletService.saveModel(topicModel);

        for (int t = 0; t < numTopics; t++) {
            List<String> topWords = new ArrayList<>();
            for (Object obj : topicModel.getTopWords(numTopWords)[t]) {
                topWords.add((String) obj);
            }
            String topicName = "topic_" + t;
            malletService.saveTopic(new Topic(topicName, topWords));
        }
    }

    @GetMapping("/topics")
    public void getTopics() throws Exception {
        List<Doc> docs = malletService.getAllRawDocs();
        if (docs.isEmpty()) { return; }
        ParallelTopicModel topicModel = malletService.loadModel();
        SerialPipes pipes = malletService.loadPipes();
        InstanceList instances = new InstanceList(pipes);

        for (Doc doc : docs) {
            instances.addThruPipe(new Instance(doc.getRawContent(), doc.getArticleId(), doc.getDate(), ""));
        }

        TopicInferencer topicInferencer = topicModel.getInferencer();

        for (int i = 0; i < instances.size(); i++) {
            Instance instance = instances.get(i);
            Doc doc = docs.get(i);
            double[] topics = topicInferencer.getSampledDistribution(instance, 300, 1, 5);

            malletService.saveProcessedDoc(new ProcessedDoc(doc.getArticleId(), doc.getTitle(), doc.getDate(), doc.getRawContent(), topics));
        }
    }

    private static SerialPipes createPipes(InputStream stoplistInputStream) throws IOException {
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(new TokenSequenceRemoveStopwords(stoplistInputStream, "UTF-8", false, false, false));
        pipeList.add(new TokenSequence2FeatureSequence());

        return new SerialPipes(pipeList);
    }

    private static InstanceList createInstanceList(InputStream dataInputStream, SerialPipes pipeList) throws IOException {

        InstanceList instances = new InstanceList(pipeList);

        instances.addThruPipe(new DocumentIterator(dataInputStream));

        return instances;
    }

    private static ParallelTopicModel trainTopicModel(InstanceList instances, int numTopics, int numIterations, int numTopWords) throws IOException {

        ParallelTopicModel topicModel = new ParallelTopicModel(numTopics);

        topicModel.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        // statistics after every iteration.
        topicModel.setNumThreads(2);

        // Train the model for <numIterations> iterations and stop
        topicModel.setNumIterations(numIterations);
        topicModel.setTopicDisplay(100, numTopWords);

        topicModel.estimate();

        return topicModel;

    }


    private void processDocuments() {
        List<Doc> documents = malletService.getAllRawDocs();
        int trainingSetSize = Math.min(documents.size(), 500);
        List<Doc> trainingSet = new ArrayList<>(trainingSetSize);

        Collections.shuffle(documents); // shuffle in-place
        trainingSet.addAll(documents.subList(0, trainingSetSize));

        try (BufferedWriter writer = Files.newBufferedWriter(malletService.getTrainDataPath())) {
            for (Doc doc : trainingSet) {
                // MALLET format: "docId date text"
                String line = String.join("\t",
                        doc.getArticleId(),
                        doc.getDate(),
                        doc.getTitle(),
                        doc.getRawContent().replaceAll("\\s+", " ")
                );
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write MALLET input file", e);
        }
    }
}
