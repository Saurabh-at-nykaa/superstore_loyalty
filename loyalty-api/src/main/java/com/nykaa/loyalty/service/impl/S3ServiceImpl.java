package com.nykaa.loyalty.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.nykaa.loyalty.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Service
@Slf4j
public class S3ServiceImpl implements S3Service {

    @Autowired
    private AmazonS3 s3Client;

    public List<CSVRecord> getCsvFile(String bucket, String key) {
        S3Object s3Object = s3Client.getObject(bucket, key);
        InputStream inputStream = s3Object.getObjectContent().getDelegateStream();
        Reader reader = new InputStreamReader(inputStream);
        try {
            CSVParser parser = CSVFormat.DEFAULT.withIgnoreHeaderCase().withFirstRecordAsHeader().withTrim().parse(reader);
            List<CSVRecord> records = parser.getRecords();
            parser.close();
            reader.close();
            inputStream.close();
            return records;
        } catch (IOException e) {
            log.error("Error occured while reading file from S3: {}", e.getMessage());
        }
        return null;
    }
}
