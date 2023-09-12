package com.nykaa.loyalty.config;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!dev")
public class S3Config {

    @Value("${aws.sqs.region}")
    private String region;

    @Bean("s3Client")
    public AmazonS3 s3Client() {
        return AmazonS3ClientBuilder.standard().withRegion(Regions.valueOf(region)).build();
    }

}