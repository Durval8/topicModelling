package com.topicmodelling.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;
    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();
    private String bucketName;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
        this.bucketName = System.getenv("BUCKET_NAME");
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