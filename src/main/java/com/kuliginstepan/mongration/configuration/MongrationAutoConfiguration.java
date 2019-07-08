package com.kuliginstepan.mongration.configuration;

import com.kuliginstepan.mongration.Mongration;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.SessionSynchronization;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration} for Mongration support.
 * Registers a {@link Mongration} bean and {@link MongrationBeanPostProcessor} bean post processor.
 * {@link MongrationProperties} used to configure a {@link Mongration}
 * {@link MongrationAutoConfiguration} may be disabled with mongration.enabled property
 */
@Configuration
@AutoConfigureAfter(MongoAutoConfiguration.class)
@AutoConfigureBefore(MongoDataAutoConfiguration.class)
@EnableConfigurationProperties(MongrationProperties.class)
@ConditionalOnMissingBean(Mongration.class)
@ConditionalOnProperty(value = MongrationProperties.ENABLED_PROPERTY, matchIfMissing = true)
@Slf4j
public class MongrationAutoConfiguration {


    private final MongrationProperties mongrationProperties;
    private final MongoClient client;
    private final MongoTransactionManager transactionManager;
    private final String database;

    public MongrationAutoConfiguration(
        ObjectProvider<MongoClientOptions> options, Environment environment,
        MongoProperties properties, MongrationProperties mongrationProperties) {
        database = properties.getDatabase() == null ? new MongoClientURI(properties.getUri()).getDatabase()
            : properties.getDatabase();
        client = new MongoClientFactory(properties, environment).createMongoClient(options.getIfAvailable());
        transactionManager = new MongoTransactionManager(new SimpleMongoDbFactory(client, database));
        this.mongrationProperties = mongrationProperties;
    }

    @Bean
    public static MongrationBeanPostProcessor mongrationBeanPostProcessor() {
        return new MongrationBeanPostProcessor();
    }

    @Bean
    public Mongration mongration() {
        MongoTemplate template = new MongoTemplate(client, database);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        template.setSessionSynchronization(SessionSynchronization.ALWAYS);
        log.info("configured Mongration with properties: {}", mongrationProperties);
        Mongration mongration = new Mongration(template, txTemplate, mongrationProperties);
        mongration.setClient(client);
        return mongration;
    }
}
