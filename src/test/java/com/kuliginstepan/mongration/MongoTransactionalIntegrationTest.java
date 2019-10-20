package com.kuliginstepan.mongration;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@ContextConfiguration(classes = MongoTransactionalIntegrationTest.class, initializers = MongoTransactionalIntegrationTest.Initializer.class)
public class MongoTransactionalIntegrationTest {

    public static final GenericContainer MONGO_DB;

    static {
        MONGO_DB = new GenericContainer<>("kuliginstepan/mongo:4.2-rs")
            .waitingFor(Wait.forHealthcheck())
            .withEnv("MONGO_INITDB_DATABASE", "test")
            .withExposedPorts(27017);
        MONGO_DB.start();
    }

    public static class Initializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                String.format("spring.data.mongodb.uri=mongodb://localhost:%d/test", MONGO_DB.getFirstMappedPort())
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}
