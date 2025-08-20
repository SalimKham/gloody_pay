package com.zsoft.gloody.gloodypayroll.configs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonConfiguration {
    @Value("${aws.s3.access-key}")
    private String awsAccessKey;

    @Value("${aws.s3.secret-key}")
    private String awsSecretKey;

    private AWSStaticCredentialsProvider getAWSStaticCredentialsProvider() {
        String accessKey = (awsAccessKey != null && !awsAccessKey.isEmpty()) ? awsAccessKey :
                System.getProperty("AWS_ACCESS_KEY_ID");
        String secretKey = (awsSecretKey != null && !awsSecretKey.isEmpty()) ? awsSecretKey :
                System.getProperty("AWS_SECRET_ACCESS_KEY");
        if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            return null;
        }
        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return new AWSStaticCredentialsProvider(basicAWSCredentials);
    }

    @Bean
    public AmazonS3 s3() {
        AWSStaticCredentialsProvider credentialsProvider = getAWSStaticCredentialsProvider();
        if (credentialsProvider == null) {
            return null;
        }
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.EU_WEST_3)
                .withCredentials(credentialsProvider)
                .build();
    }
}
