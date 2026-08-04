package com.kuliginstepan.mongration.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration(after = {DataMongoAutoConfiguration.class, DataMongoReactiveAutoConfiguration.class})
@ConditionalOnProperty(value = MongrationProperties.ENABLED_PROPERTY, matchIfMissing = true)
@Import({MongrationConfiguration.class, ReactiveMongrationConfiguration.class})
public class MongrationAutoConfiguration {

    @Bean
    public MongrationProperties mongrationProperties() {
        return new MongrationProperties();
    }

}
