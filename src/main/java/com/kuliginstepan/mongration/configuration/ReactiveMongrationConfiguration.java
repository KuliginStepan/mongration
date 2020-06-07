package com.kuliginstepan.mongration.configuration;

import com.kuliginstepan.mongration.AbstractMongration;
import com.kuliginstepan.mongration.ReactiveMongration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMongrationMode(mode = MongrationMode.REACTIVE)
@ConditionalOnBean({ReactiveMongoTemplate.class, ReactiveMongoDatabaseFactory.class})
public class ReactiveMongrationConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AbstractMongration reactiveMongration(MongrationProperties properties, ReactiveMongoTemplate mongoTemplate, ApplicationContext context) {
        return new ReactiveMongration(properties, mongoTemplate, context);
    }
}
