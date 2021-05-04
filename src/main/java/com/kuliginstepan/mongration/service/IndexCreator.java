package com.kuliginstepan.mongration.service;

import java.util.List;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import reactor.core.publisher.Mono;

/**
 * Component, which is able to create indexes for persistent entities
 */
public interface IndexCreator {

    /**
     * Creates indexes for specified persistent entity
     *
     * @param type persistent entity class, e.g. class annotated {@link org.springframework.data.mongodb.core.mapping.Document}
     * @return empty {@link Mono}
     */
    Mono<Void> createIndexes(Class<?> type);

    /**
     * Creates indexes for specified persistent entity
     *
     * @param type persistent entity class, e.g. class annotated {@link org.springframework.data.mongodb.core.mapping.Document}
     * @param collection entity collection name
     * @return empty {@link Mono}
     */
    Mono<Void> createIndexes(Class<?> type, String collection);

    /**
     * Creates indexes for specified persistent entity
     *
     * @param definitions index definitions
     * @param collection entity collection name
     * @return empty {@link Mono}
     */
    Mono<Void> createIndexes(List<IndexDefinition> definitions, String collection);

    /**
     * Creates indexes for all persistent entities presented in {@link org.springframework.data.mongodb.core.mapping.MongoMappingContext}
     *
     * @return empty {@link Mono}
     */
    Mono<Void> createIndexes();
}
