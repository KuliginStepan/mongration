package com.kuliginstepan.mongration;

import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.service.impl.ReactiveChangeSetService;
import com.kuliginstepan.mongration.service.impl.ReactiveIndexCreatorImpl;
import com.kuliginstepan.mongration.service.impl.ReactiveLockServiceImpl;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;

public class ReactiveMongration extends AbstractMongration {

    public ReactiveMongration(MongrationProperties properties, ReactiveMongoTemplate mongoTemplate) {
        super(
            new ReactiveChangeSetService(properties, mongoTemplate),
            new ReactiveIndexCreatorImpl(mongoTemplate.getConverter().getMappingContext(), mongoTemplate::indexOps),
            new ReactiveLockServiceImpl(properties.getChangelogsCollection(), mongoTemplate),
            properties
        );
    }

    @Override
    protected Mono<Object> executeChangeSetMethod(Object changelog, Method changesetMethod) {
        var parameterBeans = Arrays.stream(changesetMethod.getParameterTypes())
            .map(context::getBean)
            .toArray();

        return (Mono<Object>) ReflectionUtils.invokeMethod(changesetMethod, changelog, parameterBeans);
    }
}
