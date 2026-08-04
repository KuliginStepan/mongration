package com.kuliginstepan.mongration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kuliginstepan.mongration.TransactionalMongrationTest.TestChangeLog;
import com.kuliginstepan.mongration.TransactionalMongrationTest.TestConfig;
import com.kuliginstepan.mongration.annotation.Changelog;
import com.kuliginstepan.mongration.annotation.Changeset;
import com.kuliginstepan.mongration.configuration.MongrationAutoConfiguration;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration;
import org.springframework.boot.webflux.autoconfigure.HttpHandlerAutoConfiguration;
import org.springframework.boot.reactor.netty.autoconfigure.NettyReactiveWebServerAutoConfiguration;
import org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.Transactional;

@ImportAutoConfiguration({
    TransactionAutoConfiguration.class,
    MongoAutoConfiguration.class,
    DataMongoAutoConfiguration.class,
    PropertyPlaceholderAutoConfiguration.class,
    MongrationAutoConfiguration.class,
    NettyReactiveWebServerAutoConfiguration.class,
    HttpHandlerAutoConfiguration.class,
    WebFluxAutoConfiguration.class
})
@Import({TestChangeLog.class, TestConfig.class})
class TransactionalMongrationTest extends MongoIntegrationTest {

    @Changelog
    public static class TestChangeLog {

        @Changeset(author = "Stepan", order = 0)
        public void testChangeSet0(MongoTemplate template) {
            template.createCollection("test");
        }

        @Changeset(author = "Stepan", order = 1)
        @Transactional
        public void testChangeSet(MongoTemplate template) {
            template.save(new Document("key", "value"), "test");
            if (true) {
                throw new RuntimeException();
            }
            template.save(new Document("key", "value1"), "test");
        }

    }

    @Configuration
    public static class TestConfig {

        @Bean
        public MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory mongoDbFactory) {
            return new MongoTransactionManager(mongoDbFactory);
        }
    }

    @Test
    void shouldRollbackTransactionalChangeSet() {
        assertThrows(MongrationException.class, () -> {
            new SpringApplicationBuilder()
                .initializers(new Initializer())
                .properties(
                    "mongration.mode=imperative",
                    "mongration.changelogsCollection=test_collection",
                    "server.port=0"
                )
                .sources(TransactionalMongrationTest.class)
                .build().run();
        });
        new ApplicationContextRunner()
            .withInitializer(new Initializer())
            .withConfiguration(AutoConfigurations.of(MongoAutoConfiguration.class, DataMongoAutoConfiguration.class))
            .run(context -> {
                MongoTemplate template = context.getBean(MongoTemplate.class);
                assertThat(template.findAll(Document.class, "test").size()).isEqualTo(0);
                assertThat(template.findAll(Document.class, "test_collection").size()).isEqualTo(1);
            });
    }

    @AfterEach
    void tearDown() {
        new ApplicationContextRunner()
            .withInitializer(new Initializer())
            .withConfiguration(AutoConfigurations.of(MongoAutoConfiguration.class, DataMongoAutoConfiguration.class))
            .run(context -> {
                MongoTemplate template = context.getBean(MongoTemplate.class);
                template.dropCollection("test");
                template.dropCollection("test_collection");
            });
    }
}