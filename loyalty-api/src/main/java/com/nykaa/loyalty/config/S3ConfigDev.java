package com.nykaa.loyalty.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class S3ConfigDev {

    @Bean("s3Client")
    public AmazonS3 s3Client() {
        BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(
                "ACCESS_KEY",
                "ACESSS_SECRET",
                "TOKEN");
        return AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTH_1)
                .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
                .build();
    }
}
