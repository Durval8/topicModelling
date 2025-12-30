package com.topicmodelling;

import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
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
import java.util.regex.Pattern;

@Service
public class MalletService  {

    @Autowired
    private DocRepository docRepository;
    @Autowired
    private ProcessedDocRepository processedDocRepository;
    @Autowired
    private TopicRepository topicRepository;
    private final MongoTemplate mongoTemplate;
    private final MalletConfig config;

    public MalletService(MalletConfig config, MongoTemplate mongoTemplate) {
        this.config = config;
        this.mongoTemplate = mongoTemplate;

        if (!Files.exists(Paths.get(config.getPipesPath(), "pipes.ser"))) {
            createPipes();
        }
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

    public void saveProcessedDoc(ProcessedDoc processedDoc) { processedDocRepository.save(processedDoc); }

    public void saveTopic(Topic topic) { topicRepository.save(topic); }
}
