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
        basePackages = "com.nykaa.cs.repository",
        entityManagerFactoryRef = "adminUMEntityManagerFactory",
        transactionManagerRef = "adminUMTransactionManager"
)
public class AdminUMConfig {
    @Bean(name = "adminDataSource")
    @ConfigurationProperties(prefix = "admin.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "adminUMEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean adminUMEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("adminDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource).packages("com.nykaa.cs.entity")
                .persistenceUnit("adminPU").build();
    }

    @Bean(name = "adminUMTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("adminUMEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}