package com.kuliginstepan.mongration.configuration;

import com.kuliginstepan.mongration.Mongration;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.SessionSynchronization;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration} for Mongration support.
 * Registers a {@link Mongration} bean and {@link MongrationBeanPostProcessor} bean post processor.
 * Registers a {@link MongoTransactionManager} bean if no other beans of the same type are configured.
 * {@link MongrationProperties} used to configure a {@link Mongration}
 * {@link MongrationAutoConfiguration} may be disabled with mongration.enabled property
 */
@Configuration
@AutoConfigureAfter(MongoAutoConfiguration.class)
@AutoConfigureBefore(MongoDataAutoConfiguration.class)
@EnableConfigurationProperties(MongrationProperties.class)
@ConditionalOnMissingBean(Mongration.class)
@ConditionalOnProperty(value = MongrationProperties.ENABLED_PROPERTY, matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class MongrationAutoConfiguration {


    private final MongoClient client;
    private final MongoProperties properties;
    private final MongrationProperties mongrationProperties;

    @Bean
    public static MongrationBeanPostProcessor mongrationBeanPostProcessor() {
        return new MongrationBeanPostProcessor();
    }

    @Bean
    public Mongration mongration() {
        MongoTemplate template = new MongoTemplate(client, getDatabase());
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager());
        template.setSessionSynchronization(SessionSynchronization.ALWAYS);
        log.info("configured Mongration with properties: {}", mongrationProperties);
        return new Mongration(template, txTemplate, mongrationProperties);
    }

    @Bean
    @ConditionalOnMissingBean(MongoTransactionManager.class)
    public MongoTransactionManager transactionManager() {
        return new MongoTransactionManager(new SimpleMongoDbFactory(client, getDatabase()));
    }

    private String getDatabase() {
        return properties.getDatabase() == null ? new MongoClientURI(properties.getUri()).getDatabase()
            : properties.getDatabase();
    }
}
