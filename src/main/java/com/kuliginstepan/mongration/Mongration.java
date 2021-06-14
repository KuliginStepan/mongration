package com.kuliginstepan.mongration;

import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.service.impl.ChangeSetService;
import com.kuliginstepan.mongration.service.impl.IndexCreatorImpl;
import com.kuliginstepan.mongration.service.impl.LockServiceImpl;
import java.lang.reflect.Method;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;

public class Mongration extends AbstractMongration {

    public Mongration(MongrationProperties properties, MongoTemplate template, ApplicationContext context) {

        super(
            new ChangeSetService(properties, template),
            new IndexCreatorImpl(template.getConverter().getMappingContext(), template),
            new LockServiceImpl(properties.getChangelogsCollection(), template),
            properties,
            context
        );
    }

    @Override
    protected Mono<Object> executeChangeSetMethod(Method changesetMethod, Object changelog, Object[] parameters) {
        try {
            return Mono.justOrEmpty(ReflectionUtils.invokeMethod(changesetMethod, changelog, parameters));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
