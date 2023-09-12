package com.nykaa.loyalty.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.nykaa.loyalty.repository.read",
        entityManagerFactoryRef = "loyaltyReadEntityManagerFactory",
        transactionManagerRef = "loyaltyReadTransactionManager"
)
public class LoyaltyReadDatasource {

    @Bean(name = "loyaltyReadDataSource")
    @ConfigurationProperties(prefix = "read-replica.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "loyaltyReadEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("loyaltyReadDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.nykaa.loyalty.entity")
                .persistenceUnit("loyaltyReadPU")
                .build();
    }

    @Bean(name = "loyaltyReadTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("loyaltyReadEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
