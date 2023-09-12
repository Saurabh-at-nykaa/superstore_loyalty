package com.nykaa.loyalty.service;

import org.apache.commons.csv.CSVRecord;

import java.util.List;

public interface S3Service {

    List<CSVRecord> getCsvFile(String bucket, String key);

}
