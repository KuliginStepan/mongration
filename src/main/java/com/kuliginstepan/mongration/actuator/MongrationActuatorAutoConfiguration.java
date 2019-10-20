package com.kuliginstepan.mongration.actuator;

import com.kuliginstepan.mongration.configuration.MongrationAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Endpoint.class)
@AutoConfigureAfter(MongrationAutoConfiguration.class)
public class MongrationActuatorAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(MongoTemplate.class)
    @ConditionalOnMissingBean(ReactiveMongoTemplate.class)
    @ConditionalOnAvailableEndpoint(endpoint = MongrationEndpoint.class)
    public static class MongrationEndpointConfiguration {

        @Bean
        public MongrationEndpoint mongrationEndpoint(MongoTemplate template) {
            return new MongrationEndpoint(template);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(ReactiveMongoTemplate.class)
    @ConditionalOnAvailableEndpoint(endpoint = ReactiveMongrationEndpoint.class)
    public static class ReactiveMongrationEndpointConfiguration {

        @Bean
        public ReactiveMongrationEndpoint reactiveMongrationEndpoint(ReactiveMongoTemplate template) {
            return new ReactiveMongrationEndpoint(template);
        }
    }
}
