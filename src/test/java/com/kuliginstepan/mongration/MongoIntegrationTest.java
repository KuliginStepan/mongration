package com.kuliginstepan.mongration;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;

@ContextConfiguration(classes = MongoIntegrationTest.class, initializers = MongoIntegrationTest.Initializer.class)
public class MongoIntegrationTest {

    static final GenericContainer MONGO_DB;

    static {
        MONGO_DB = new GenericContainer<>("mongo:4.2")
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
