package com.kuliginstepan.mongration.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Annotation provides {@link MongrationConfiguration} instead of {@link MongrationAutoConfiguration}. It`s recommended
 * to use with {@link EnableMongoRepositories} to avoid unexpected behavior with bean initialization order
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MongrationConfiguration.class)
public @interface EnableMongration {
}
