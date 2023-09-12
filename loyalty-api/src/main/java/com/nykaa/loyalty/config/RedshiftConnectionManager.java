package com.nykaa.loyalty.config;

import com.nykaa.loyalty.enums.ErrorCodes;
import com.nykaa.loyalty.exception.LoyaltyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
@Component
public class RedshiftConnectionManager {

    @Value("${datawarehouse.jdbc.url}")
    private String JDBC_URL;
    @Value("${datawarehouse.jdbc.username}")
    private String USERNAME;
    @Value("${datawarehouse.jdbc.password}")
    private String PASSWORD;
    @Value("${datawarehouse.jdbc.className}")
    private String CLASS_NAME;

    private Connection connection = null;

    public void connect() throws Exception {
        // Dynamically load driver at runtime.
        try {
            Class.forName(CLASS_NAME);
        } catch (ClassNotFoundException e) {
            log.error("JDBC Redshift driver class not found");
            throw new Exception("Exception while connecting to redshift with message : " + e.getMessage());
        }
        Properties properties = getPropertiesForDriverManager();
        try {
            log.info("Attempting to connect with redshift");
            this.connection = DriverManager.getConnection(JDBC_URL, properties);
            log.info("Redshift Connection successful");
        } catch (SQLException e) {
            log.error("Connection with redshift failed with reason : {}", e.getMessage());
            throw new LoyaltyException(ErrorCodes.REDSHIFT_CONNECTION_ERROR);
        }
    }

    public Connection getConnection() throws Exception {
        if (null == this.connection) {
            connect();
        }
        return this.connection;
    }

    private Properties getPropertiesForDriverManager() {
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        return props;
    }
}