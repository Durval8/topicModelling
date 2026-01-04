package com.topicmodelling.mallet;

import cc.mallet.topics.ParallelTopicModel;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;
    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();
    private final String bucketName;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
        this.bucketName = System.getenv("BUCKET_NAME");
    }

    public void uploadModel(ParallelTopicModel parallelTopicModel, String modelID) {
        byte[] bytes = serializeModel(parallelTopicModel);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/octet-stream");
        metadata.setContentLength(bytes.length);

        PutObjectRequest request = new PutObjectRequest(
                bucketName,
                "models/" + modelID + ".bin",
                new ByteArrayInputStream(bytes),
                metadata
        );

        amazonS3.putObject(request);
    }

    private byte[] serializeModel(ParallelTopicModel parallelTopicModel) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(parallelTopicModel);
            oos.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize model", e);
        }
    }

    // Upload file to S3 bucket
    public void uploadFile(Doc doc) {
        try {
            byte[] json = om.writeValueAsBytes(doc); // JSON, not Java serialization
            var meta = new ObjectMetadata();
            meta.setContentType("application/json");
            meta.setContentLength(json.length);
            System.out.println(doc.getArticleId());
            var req = new PutObjectRequest(bucketName, "raw_docs/" + doc.getArticleId(), new ByteArrayInputStream(json), meta);
            amazonS3.putObject(req);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Error uploading file");
            // return "Error uploading file";
        }
        //return "200";
    }

    // Download file from S3 bucket
    public S3Object downloadFile(String fileName) {
        return amazonS3.getObject(bucketName, fileName);
    }
}