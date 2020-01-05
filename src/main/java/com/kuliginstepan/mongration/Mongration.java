package com.kuliginstepan.mongration;

import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.service.impl.ChangeSetService;
import com.kuliginstepan.mongration.service.impl.IndexCreatorImpl;
import com.kuliginstepan.mongration.service.impl.LockServiceImpl;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;

public class Mongration extends AbstractMongration {

    public Mongration(MongrationProperties properties, MongoTemplate template) {

        super(
            new ChangeSetService(properties, template),
            new IndexCreatorImpl(template.getConverter().getMappingContext(), template),
            new LockServiceImpl(properties.getChangelogsCollection(), template),
            properties
        );
    }

    @Override
    protected Mono<Object> executeChangeSetMethod(Object changelog, Method changesetMethod) {
        var parameterBeans = Arrays.stream(changesetMethod.getParameterTypes())
            .map(context::getBean)
            .toArray();

        try {
            return Mono.justOrEmpty(ReflectionUtils.invokeMethod(changesetMethod, changelog, parameterBeans));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
