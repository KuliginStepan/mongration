package com.kuliginstepan.mongration.configuration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration provides {@link MongrationConfiguration}. It configures after {@link MongrationAutoConfiguration}
 * and before {@link MongoDataAutoConfiguration}.
 */
@Configuration
@AutoConfigureAfter(MongoAutoConfiguration.class)
@AutoConfigureBefore(MongoDataAutoConfiguration.class)
@Import(MongrationConfiguration.class)
public class MongrationAutoConfiguration {

}
