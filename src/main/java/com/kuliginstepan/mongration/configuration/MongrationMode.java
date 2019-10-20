package com.kuliginstepan.mongration.configuration;

/**
 * Mode for running change sets
 */
public enum MongrationMode {

    /**
     * Runs change sets on imperative stack, e. g. with {@link org.springframework.data.mongodb.core.MongoTemplate}
     */
    IMPERATIVE,

    /**
     * Runs change sets on reactive stack, e. g. with {@link org.springframework.data.mongodb.core.ReactiveMongoTemplate}
     */
    REACTIVE,

    /**
     * Runs change sets depending on changeset's return values. {@link OnMongrationModeCondition}
     */
    AUTO
}
