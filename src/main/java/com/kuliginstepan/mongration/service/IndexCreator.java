package com.kuliginstepan.mongration.service;

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
     * Creates indexes for all persistent entities presented in {@link org.springframework.data.mongodb.core.mapping.MongoMappingContext}
     *
     * @return empty {@link Mono}
     */
    Mono<Void> createIndexes();
}
