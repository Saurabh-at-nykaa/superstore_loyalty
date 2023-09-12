package com.nykaa.loyalty.service.helper;

import com.nykaa.loyalty.config.RedshiftConnectionManager;
import com.nykaa.loyalty.enums.ErrorCodes;
import com.nykaa.loyalty.exception.LoyaltyException;
import com.nykaa.loyalty.util.Constants;
import com.nykaa.loyalty.util.SystemPropertyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLTimeoutException;
import java.sql.Statement;

@Component
@Slf4j
public class RedshiftQueryHelper {

    @Autowired
    private RedshiftConnectionManager connManager;

    public ResultSet executeQueryAndReturnResult(String query) throws Exception {
        Connection connection = connManager.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            if (!connection.isValid(10)) {
                connManager.connect();
            }
            statement = connection.createStatement();
            statement.setQueryTimeout(Integer.valueOf(SystemPropertyUtil.getProperty(
                    Constants.Dwh.TIMEOUT, Constants.Dwh.DEFAULT_TIMEOUT)));
            resultSet = statement.executeQuery(query);
        } catch (SQLTimeoutException sqlTimeoutException) {
            log.error("Redshift query execution timeout happened for query : {}", query);
            throw new LoyaltyException(ErrorCodes.REDSHIFT_QUERY_TIMEOUT);
        }
        catch (Exception sqlEx) {
            log.error("Redshift connection closed {}", sqlEx.getMessage());
            throw new LoyaltyException(ErrorCodes.REDSHIFT_CONNECTION_ERROR);
        }
        return resultSet;
    }
}
