package com.kuliginstepan.mongration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kuliginstepan.mongration.ReactiveTransactionalMongrationTest.TestChangeLog;
import com.kuliginstepan.mongration.ReactiveTransactionalMongrationTest.TestConfig;
import com.kuliginstepan.mongration.annotation.Changelog;
import com.kuliginstepan.mongration.annotation.Changeset;
import com.kuliginstepan.mongration.configuration.MongrationAutoConfiguration;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@SpringBootConfiguration
@ImportAutoConfiguration({TransactionAutoConfiguration.class, MongoReactiveAutoConfiguration.class,
    MongoReactiveDataAutoConfiguration.class,
    PropertyPlaceholderAutoConfiguration.class, MongrationAutoConfiguration.class})
@Import({TestChangeLog.class, TestConfig.class})
class ReactiveTransactionalMongrationTest extends MongoTransactionalIntegrationTest {

    @Test
    void shouldRollbackTransactionalChangeSet() {
        assertThrows(RuntimeException.class, () -> {
            new SpringApplicationBuilder()
                .initializers(new Initializer())
                .properties(
                    "mongration.mode=reactive",
                    "mongration.changelogsCollection=test_collection",
                    "logging.level.com.kuliginstepan.mongration=TRACE"
                )
                .sources(ReactiveTransactionalMongrationTest.class)
                .build().run();
        });
        new ApplicationContextRunner()
            .withInitializer(new Initializer())
            .withConfiguration(
                AutoConfigurations.of(MongoReactiveAutoConfiguration.class, MongoReactiveDataAutoConfiguration.class))
            .run(context -> {
                ReactiveMongoTemplate template = context.getBean(ReactiveMongoTemplate.class);
                assertThat(template.findAll(Document.class, "test").collectList().block().size()).isEqualTo(0);
                assertThat(template.findAll(Document.class, "test_collection").collectList().block().size())
                    .isEqualTo(1);
            });
    }

    @AfterEach
    void tearDown() {
        new ApplicationContextRunner()
            .withInitializer(new Initializer())
            .withConfiguration(
                AutoConfigurations.of(MongoReactiveAutoConfiguration.class, MongoReactiveDataAutoConfiguration.class))
            .run(context -> {
                ReactiveMongoTemplate template = context.getBean(ReactiveMongoTemplate.class);
                template.dropCollection("test").block();
                template.dropCollection("test_collection").block();
            });
    }

    @Changelog
    public static class TestChangeLog {

        @Changeset(author = "Stepan", order = 0)
        public Mono<Void> testChangeSet0(ReactiveMongoTemplate template) {
            return template.createCollection("test").then();
        }

        @Changeset(author = "Stepan", order = 1)
        @Transactional
        public Mono<Void> testChangeSet(ReactiveMongoTemplate template) {
            return template.save(new Document("key", "value"), "test")
                .then(Mono.error(new RuntimeException("Sample exception!")));
        }

    }

    @Configuration
    public static class TestConfig {

        @Bean
        public ReactiveMongoTransactionManager mongoTransactionManager(ReactiveMongoDatabaseFactory factory) {
            return new ReactiveMongoTransactionManager(factory);
        }
    }
}
