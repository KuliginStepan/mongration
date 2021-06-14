package com.kuliginstepan.mongration;

import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.service.impl.ReactiveChangeSetService;
import com.kuliginstepan.mongration.service.impl.ReactiveIndexCreatorImpl;
import com.kuliginstepan.mongration.service.impl.ReactiveLockServiceImpl;
import java.lang.reflect.Method;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;

public class ReactiveMongration extends AbstractMongration {

    public ReactiveMongration(MongrationProperties properties, ReactiveMongoTemplate mongoTemplate, ApplicationContext context) {
        super(
            new ReactiveChangeSetService(properties, mongoTemplate),
            new ReactiveIndexCreatorImpl(mongoTemplate.getConverter().getMappingContext(), mongoTemplate::indexOps),
            new ReactiveLockServiceImpl(properties.getChangelogsCollection(), mongoTemplate),
            properties,
            context
        );
    }

    @Override
    protected Mono<Object> executeChangeSetMethod(Method changesetMethod, Object changelog, Object[] parameters) {
        return (Mono<Object>) ReflectionUtils.invokeMethod(changesetMethod, changelog, parameters);
    }
}
