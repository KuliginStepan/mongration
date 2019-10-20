package com.kuliginstepan.mongration.configuration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({MongoDataAutoConfiguration.class, MongoReactiveDataAutoConfiguration.class})
@ConditionalOnProperty(value = MongrationProperties.ENABLED_PROPERTY, matchIfMissing = true)
@Import({MongrationConfiguration.class, ReactiveMongrationConfiguration.class})
public class MongrationAutoConfiguration {

    @Bean
    public MongrationProperties mongrationProperties() {
        return new MongrationProperties();
    }

}
