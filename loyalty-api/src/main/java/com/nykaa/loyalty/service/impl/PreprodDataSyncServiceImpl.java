package com.nykaa.loyalty.service.impl;

import com.nykaa.loyalty.service.PreprodDataSyncService;
import com.nykaa.loyalty.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

@Slf4j
@Service
public class PreprodDataSyncServiceImpl implements PreprodDataSyncService {

    @Autowired
    private Environment env;

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    @Override
    public void syncData(int minutes) throws Exception {
        if (!Arrays.asList(env.getActiveProfiles()).contains(Constants.Environment.PREPROD)) {
            log.info("dwh sync not applicable for environments other than preprod");
            return;
        }
        Date updatedAt = dateFromCurrentDate(minutes);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String strDate = dateFormat.format(updatedAt);
        // sync nykaad_customer_info_view
        try {
            connection = getDBConnection(Constants.MagentoDBDetails.url, Constants.MagentoDBDetails.username,
                    Constants.MagentoDBDetails.password, Constants.MagentoDBDetails.className);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select customer_id, business_type, status from bulk_buyer_documents" +
                    " where updated_at > '"+dateFormat.format(DateUtils.addMinutes(updatedAt, 330))+ "';");
            Connection dwhConnection = getDBConnection(env.getProperty("datawarehouse.jdbc.url"), env.getProperty("datawarehouse.jdbc.username"),
                    env.getProperty("datawarehouse.jdbc.password"), env.getProperty("datawarehouse.jdbc.className"));
            statement = dwhConnection.createStatement();
            while (resultSet.next()) {
                ResultSet resultSetExists = statement.executeQuery("Select customer_id from devops.nykaad_customer_info_view_beauty where customer_id = "+resultSet.getInt("customer_id")+";");
                if (!resultSetExists.next()) {
                    statement.executeUpdate("Insert into devops.nykaad_customer_info_view_beauty (customer_id,business_type,status)" +
                            " VALUES (" + resultSet.getInt("customer_id") + "," + resultSet.getString("business_type")
                            + "," + resultSet.getInt("status") + ");");
                }
            }

            // sync fact_order_view
            Connection preprodConnection = getDBConnection(env.getProperty("preprod-oms.datasource.url"), env.getProperty("preprod-oms.datasource.username"),
                    env.getProperty("preprod-oms.datasource.password"), env.getProperty("preprod-oms.datasource.className"));
            statement = preprodConnection.createStatement();
            resultSet = statement.executeQuery("Select ocd.user_id, o.magento_created_at, o.magento_order_no, o.source, o.coupon_code, ocd.address_line1\n" +
                    "from oms.orders as o join oms.order_customer_detail as ocd on ocd.order_id = o.id where o.source = 'nykaad'" +
                    " and o.updated > '"+strDate+ "';");
            statement = dwhConnection.createStatement();
            while (resultSet.next()) {
                ResultSet resultSetExists = statement.executeQuery("Select nykaa_orderno from devops.fact_order_view where nykaa_orderno = '"+resultSet.getString("magento_order_no")+"';");
                if (!resultSetExists.next()) {
                    statement.executeUpdate("Insert into devops.fact_order_view (order_customerid,order_date,nykaa_orderno,is_test_order,eretail_orderno,order_source,coupon_code,order_addressline,data_source_id,order_dt_cancelled)" +
                            " VALUES ('" + resultSet.getString("user_id") + "','" + resultSet.getDate("magento_created_at")
                            + "','" + resultSet.getString("magento_order_no") + "'," + 0 + ",'" + resultSet.getString("magento_order_no") + "','"
                            + resultSet.getString("source") + "','" + resultSet.getString("coupon_code") + "','"
                            + resultSet.getString("address_line1") + "','USER_OMS'," + null + ");");
                }
            }

            // sync fact_order_detail_new
            statement = preprodConnection.createStatement();
            resultSet = statement.executeQuery("Select SUM((so.unit_price_incl_tax * so.quantity) - so.discount) as orderdetail_total_amount, o.magento_order_no as order_no, " +
                    "so.sku, so.`free`, so.created, so.updated from oms.orders o join oms.sub_order so " +
                    "on o.id = so.order_id where o.source = 'nykaad' and o.updated > '"+strDate+ "' group by so.order_id;");
            statement = dwhConnection.createStatement();
            while (resultSet.next()) {
                ResultSet resultSetExists = statement.executeQuery("Select nykaa_orderno from beauty.fact_order_detail_new where nykaa_orderno = '"+resultSet.getString("order_no")+"';");
                if (!resultSetExists.next()) {
                    statement.executeUpdate("Insert into beauty.fact_order_detail_new (orderdetail_total_amount,nykaa_orderno,product_sku,is_free_product,data_source_id,orderdetail_dt_created,orderdetail_dt_lastupdated)" +
                            " VALUES (" + resultSet.getLong("orderdetail_total_amount") + ",'" + resultSet.getString("order_no")
                            + "','" + resultSet.getString("sku") + "'," + resultSet.getInt("free") + ",'warehouse','"+resultSet.getDate("created")+"','"+resultSet.getDate("updated")+"');");
                }
            }
        } catch (Exception sqlEx) {
            log.error("Database connection closed or timeout happened {}", sqlEx.getMessage());
            throw new Exception(sqlEx.getMessage());
        }

    }

    private Date dateFromCurrentDate(int minutes) {
        return DateUtils.addMinutes(new Date(), -minutes);
    }

    private Connection getDBConnection(String url, String username, String password, String className) throws Exception {
        Connection connection;
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("JDBC driver class not found - {}", className);
            throw new Exception("Exception while connecting to db with message : " + e.getMessage());
        }
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        try {
            log.info("Attempting to connect with database");
            connection = DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            log.error("Connection with database failed with reason : {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return connection;
    }
}
