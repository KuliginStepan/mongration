package com.kuliginstepan.mongration.configuration;

import static com.kuliginstepan.mongration.configuration.MongrationMode.AUTO;
import static com.kuliginstepan.mongration.configuration.MongrationMode.IMPERATIVE;
import static com.kuliginstepan.mongration.configuration.MongrationMode.REACTIVE;

import com.kuliginstepan.mongration.annotation.Changelog;
import com.kuliginstepan.mongration.utils.ChangelogUtils;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionMessage.Builder;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import reactor.core.publisher.Mono;

public class OnMongrationModeCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnMongrationMode.class.getName(), true);
        MongrationMode requiredType = (MongrationMode) attributes.get("mode");
        MongrationMode configuredType = context.getEnvironment().getProperty("mongration.mode", MongrationMode.class, AUTO);
        Builder messageBuilder = ConditionMessage.forCondition(ConditionalOnMongrationMode.class);

        if (configuredType == requiredType) {
            return ConditionOutcome.match(messageBuilder.because("configured type of '" + configuredType.name()
                + "' matched required type"));
        } else if (configuredType == AUTO && hasMonoChangeSets(context) && requiredType == REACTIVE) {
            return ConditionOutcome.match(messageBuilder.because("configured type AUTO and changelogs return Mono"));
        } else if (configuredType == AUTO && !hasMonoChangeSets(context) && requiredType == IMPERATIVE) {
            return ConditionOutcome
                .match(messageBuilder.because("configured type AUTO and changelogs don't return Mono"));
        }
        return ConditionOutcome.noMatch(messageBuilder.because("configured type (" + configuredType.name()
            + ") did not match required type (" + requiredType.name() + ')'));
    }

    private boolean hasMonoChangeSets(ConditionContext context) {
        return Stream.of(context.getBeanFactory().getBeanNamesForAnnotation(Changelog.class))
            .map(beanName -> context.getRegistry().getBeanDefinition(beanName))
            .map(BeanDefinition::getBeanClassName)
            .map(className -> ClassUtils.resolveClassName(className, getClass().getClassLoader()))
            .flatMap(changelog -> ChangelogUtils.findChangeSetMethods(changelog).stream())
            .anyMatch(method -> method.getReturnType().isAssignableFrom(Mono.class));
    }
}
