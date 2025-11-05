package com.topicmodelling;

import cc.mallet.pipe.SerialPipes;
import cc.mallet.topics.ParallelTopicModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class MalletService  {

    @Autowired
    private DocRepository docRepository;
    private ProcessedDocRepository processedDocRepository;
    private TopicRepository topicRepository;
    private final MalletConfig config;

    public MalletService(MalletConfig config) {
        this.config = config;
    }

    public void saveModel(ParallelTopicModel model) {
        File file = new File(config.getModelPath());
        model.write(file);
    }

    public void savePipes(SerialPipes pipes) throws IOException {
        try {
            FileOutputStream outFile = new FileOutputStream(config.getPipesPath());
            ObjectOutputStream oos = new ObjectOutputStream(outFile);
            oos.writeObject(pipes);
            oos.close();
        } catch (FileNotFoundException ex) {
            // handle error
        } catch (IOException ex) {
            // handle error
        }
    }

    public SerialPipes loadPipes() throws IOException {
        SerialPipes pipes = null;
        try {
            FileInputStream outFile = new FileInputStream("pipes.ser");
            ObjectInputStream oos = new ObjectInputStream(outFile);
            pipes = (SerialPipes) oos.readObject();
        } catch (IOException ex) {
            System.out.println("Could not read pipes from file: " + ex);
        } catch (ClassNotFoundException ex) {
            System.out.println("Could not load the pipes: " + ex);
        }
        return pipes;
    }

    public ParallelTopicModel loadModel() throws Exception {
        return ParallelTopicModel.read(new File(config.getModelPath()));
    }

    public Path getTrainDataPath() { return Paths.get(config.getCorpusPath()); }

    public List<Doc> getAllRawDocs() { return docRepository.findAll(); }

    public void saveProcessedDoc(ProcessedDoc processedDoc) { processedDocRepository.save(processedDoc); }

    public void saveTopic(Topic topic) { topicRepository.save(topic); }
}
