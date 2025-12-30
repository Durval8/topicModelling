package com.topicmodelling;

import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/mallet", produces = "application/json")
public class MalletController {

    @Autowired
    private MalletService malletService;

    @GetMapping("/train")
    public void trainTopicModels(
            @RequestParam String theme,
            @RequestParam(required = false, defaultValue = "500") int size
    ) throws IOException {

        String corpus = processDocuments(theme, size);

//        InputStream dataInputStream = new FileInputStream(malletService.getTrainDataPath(theme).toString());
        InputStream dataInputStream = new ByteArrayInputStream(corpus.getBytes(StandardCharsets.UTF_8));
        InputStream stoplistInputStream = MalletController.class.getResourceAsStream("/stoplist_en.txt");
//        SerialPipes pipeList = createPipes(stoplistInputStream);

//        malletService.savePipes(pipeList, theme);

        InstanceList instances = createInstanceList(dataInputStream, malletService.loadPipes());

        int numTopics = 10;
        int numIterations = 300;
        int numTopWords = 5;


        ParallelTopicModel topicModel = trainTopicModel(instances, numTopics, numIterations, numTopWords);
        malletService.saveModel(topicModel, theme);

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
    public void getTopics(String theme) throws Exception {
        List<Doc> docs = malletService.getAllRawDocs();
        if (docs.isEmpty()) { return; }
        ParallelTopicModel topicModel = malletService.loadModel(theme);
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

            malletService.saveProcessedDoc(new ProcessedDoc(doc.getArticleId(), doc.getTitle(), doc.getDate(), doc.getRawContent(), topics, theme));
        }
    }

//    private static SerialPipes createPipes(InputStream stoplistInputStream) throws IOException {
//        // Begin by importing documents from text to feature sequences
//        ArrayList<Pipe> pipeList = new ArrayList<>();
//
//        // Pipes: lowercase, tokenize, remove stopwords, map to features
//        pipeList.add(new CharSequenceLowercase());
//        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
//        pipeList.add(new TokenSequenceRemoveStopwords(stoplistInputStream, "UTF-8", false, false, false));
//        pipeList.add(new TokenSequence2FeatureSequence());
//
//        return new SerialPipes(pipeList);
//    }

    private static InstanceList createInstanceList(InputStream dataInputStream, SerialPipes pipeList) {

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


    private String processDocuments(String theme, int size) {
//        List<Doc> documents = malletService.getAllRawDocs();
        List<Doc> documents = malletService.getRandomRawDocs(size, theme);
        if (documents.isEmpty()) {
            throw new IllegalStateException(
                    "No documents found for theme '" + theme + "'. Training aborted."
            );
        }
//        int trainingSetSize = Math.min(documents.size(), 500);
//        List<Doc> trainingSet = new ArrayList<>(documents.size());

//        Collections.shuffle(documents); // shuffle in-place
//        trainingSet.addAll(documents.subList(0, documents.size()));
        StringBuilder sb = new StringBuilder();

//        try (BufferedWriter writer = Files.newBufferedWriter(malletService.getTrainDataPath(theme))) {
        for (Doc doc : documents) {
            System.out.println(doc.getTheme());
            // MALLET format: "docId date title text"
            String line = String.join("\t",
                    doc.getArticleId(),
                    doc.getDate(),
                    doc.getTitle(),
                    doc.getRawContent().replaceAll("\\s+", " "),
                    "\n"
            );
            sb.append(line);
//            writer.write(line);
//            writer.newLine();
        }
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to write MALLET input file", e);
//        }

        return sb.toString();
    }
}
