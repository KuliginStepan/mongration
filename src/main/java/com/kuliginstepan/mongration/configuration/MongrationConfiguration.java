package com.kuliginstepan.mongration.configuration;

import com.kuliginstepan.mongration.AbstractMongration;
import com.kuliginstepan.mongration.Mongration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMongrationMode(mode = MongrationMode.IMPERATIVE)
@ConditionalOnBean({MongoTemplate.class, MongoDbFactory.class})
public class MongrationConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AbstractMongration mongration(MongrationProperties properties, MongoTemplate mongoTemplate) {
        return new Mongration(properties, mongoTemplate);
    }
}
