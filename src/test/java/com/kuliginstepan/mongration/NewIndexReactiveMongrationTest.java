package com.kuliginstepan.mongration;

import static org.assertj.core.api.Assertions.assertThat;

import com.kuliginstepan.mongration.NewIndexReactiveMongrationTest.TestChangeLog;
import com.kuliginstepan.mongration.annotation.Changelog;
import com.kuliginstepan.mongration.annotation.Changeset;
import com.kuliginstepan.mongration.configuration.MongrationAutoConfiguration;
import com.kuliginstepan.mongration.entity.ChangesetEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@DataMongoTest(properties = {
    "mongration.mode=reactive",
    "mongration.changelogsCollection=test_collection",
    "spring.data.mongodb.auto-index-creation=false"
})
@EnableAutoConfiguration
@ImportAutoConfiguration(MongrationAutoConfiguration.class)
@Import(TestChangeLog.class)
class NewIndexReactiveMongrationTest extends MongoIntegrationTest {

    @Changelog
    public static class TestChangeLog {

        @Changeset(author = "Stepan", order = 1)
        public Mono<Void> testChangeSet(ReactiveMongoTemplate template) {
            return template.findAll(TestDocument.class)
                .flatMap(doc -> {
                    doc.setIndex(doc.getId());
                    return template.save(doc);
                })
                .then();
        }

    }

    @org.springframework.data.mongodb.core.mapping.Document
    @Data
    @AllArgsConstructor
    public static class TestDocument {

        @Id
        private String id;
        @Indexed(unique = true)
        private String index;
    }

    @Autowired
    private ReactiveMongoTemplate template;

    @BeforeAll
    static void setUp() {
        new ApplicationContextRunner()
            .withInitializer(new Initializer())
            .withConfiguration(AutoConfigurations.of(MongoReactiveAutoConfiguration.class, MongoReactiveDataAutoConfiguration.class))
            .withPropertyValues("spring.data.mongodb.auto-index-creation=false")
            .run(context -> {
                var template = context.getBean(ReactiveMongoTemplate.class);
                template.save(new TestDocument("1", null)).block();
                template.save(new TestDocument("2", null)).block();
            });
    }

    @Test
    void shouldExecuteChangeLogWithNewIndexes() {
        var changesets = template.findAll(ChangesetEntity.class, "test_collection").collectList().block();
        var documents = template.findAll(Document.class, "test").collectList().block();
        assertThat(template.indexOps(TestDocument.class).getIndexInfo().collectList().block()).hasSize(2);
    }

    @AfterEach
    void tearDown() {
        template.dropCollection("test_collection").block();
        template.dropCollection("test").block();
        template.dropCollection(TestDocument.class).block();
    }
}