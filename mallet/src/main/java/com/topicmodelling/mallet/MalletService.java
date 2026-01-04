package com.topicmodelling.mallet;

import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class MalletService  {

    @Autowired
    private DocRepository docRepository;
//    @Autowired
//    private ProcessedDocRepository processedDocRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private BatchService batchService;
    private final MongoTemplate mongoTemplate;
    private final MalletConfig config;

    public MalletService(MalletConfig config, MongoTemplate mongoTemplate) {
        this.config = config;
        this.mongoTemplate = mongoTemplate;

        if (!Files.exists(Paths.get(config.getPipesPath(), "pipes.ser"))) {
            createPipes();
        }
    }

    public void trainModel(String modelID) throws IOException {

        List<BatchEvent> batch = batchService.getBatch(modelID);

        System.out.println("Total Size: " + batch.size());

        if (batch.isEmpty()) {
            throw new IllegalStateException(
                    "No documents found for model ID '" + modelID + "'. Training aborted."
            );
        }

        InstanceList instances = new InstanceList(loadPipes());
        instances.addThruPipe(new BatchIterator(batch));

        int numTopics = 10;
        int numIterations = 300;
        int numTopWords = 5;

        ParallelTopicModel topicModel = trainTopicModel(instances, numTopics, numIterations, numTopWords);

//        saveModel(topicModel, modelID);
        s3Service.uploadModel(topicModel, modelID);

        batchService.cleanupBatch(modelID);

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

    private String genCorpus(List<BatchEvent> batchEvents) {
        StringBuilder sb = new StringBuilder();

        for(BatchEvent batchEvent : batchEvents) {
            Doc doc = batchEvent.getDoc();
            String line = String.join("\t",
                    doc.getArticleId(),
                    doc.getDate(),
                    doc.getTitle(),
                    doc.getRawContent().replaceAll("\\s+", " "),
                    "\n"
            );
            sb.append(line);
        }
        return sb.toString();
    }

    private void createPipes() {
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<>();
        InputStream stoplistInputStream = MalletController.class.getResourceAsStream("/stoplist_en.txt");

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(new TokenSequenceRemoveStopwords(stoplistInputStream, "UTF-8", false, false, false));
        pipeList.add(new TokenSequence2FeatureSequence());

        try {
            savePipes(new SerialPipes(pipeList));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening pipes file");
        }
    }

    public void saveModel(ParallelTopicModel model, String theme) throws IOException {
        Path modelPath = Paths.get(config.getModelPath(), theme, theme.concat("_model.ser"));
        if (!Files.exists(modelPath)) {
            Files.createDirectory(modelPath.getParent());
        }
        File file = new File(modelPath.toString());
        System.out.printf("MODEL FILE: %s\n", file.getAbsoluteFile());
        model.write(file);
    }

    private void savePipes(SerialPipes pipes) throws IOException {
        try {
            FileOutputStream outFile = new FileOutputStream(
                    Paths.get(config.getPipesPath(), "pipes.ser").toString()
            );
            ObjectOutputStream oos = new ObjectOutputStream(outFile);
            oos.writeObject(pipes);
            oos.close();
        } catch (IOException ex) {
            throw new FileNotFoundException("Pipes file could not be resolved");
        }
    }
//
    public SerialPipes loadPipes() throws IOException {
        SerialPipes pipes = null;
        try {
            FileInputStream outFile = new FileInputStream(
                    Paths.get(config.getPipesPath(), "pipes.ser").toString()
            );
            ObjectInputStream oos = new ObjectInputStream(outFile);
            pipes = (SerialPipes) oos.readObject();
        } catch (IOException ex) {
            System.out.println("Could not read pipes from file: " + ex);
        } catch (ClassNotFoundException ex) {
            System.out.println("Could not load the pipes: " + ex);
        }
        return pipes;
    }

    public ParallelTopicModel loadModel(String theme) throws Exception {
        return ParallelTopicModel.read(new File(Paths.get(config.getModelPath(), theme, theme.concat("_model.ser")).toString()));
    }

//    public Path getTrainDataPath(String theme) {
//        return Paths.get(config.getCorpusPath(), theme.concat(".txt"));
//    }

    public List<Doc> getAllRawDocs() { return docRepository.findAll(); }

    public List<Doc> getRandomRawDocs(int size, String theme) {
        long themeCount = mongoTemplate.count(
                Query.query(Criteria.where("theme").is(theme)),
                "raw_docs"
        );

        if (themeCount == 0) {
            return List.of();
        }

        int sampleSize = (int) Math.min(themeCount, size);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("theme").is(theme)),
                Aggregation.sample(sampleSize)
        );

        return mongoTemplate
                .aggregate(agg, "raw_docs", Doc.class)
                .getMappedResults();
    }

//    public void saveProcessedDoc(ProcessedDoc processedDoc) { processedDocRepository.save(processedDoc); }

    public void saveTopic(Topic topic) { topicRepository.save(topic); }
}
