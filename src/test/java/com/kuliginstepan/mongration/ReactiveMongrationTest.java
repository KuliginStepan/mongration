package com.kuliginstepan.mongration;

import static org.assertj.core.api.Assertions.assertThat;

import com.kuliginstepan.mongration.ReactiveMongrationTest.TestChangeLog;
import com.kuliginstepan.mongration.ReactiveMongrationTest.TestChangeLog1;
import com.kuliginstepan.mongration.ReactiveMongrationTest.TestChangeLog2;
import com.kuliginstepan.mongration.annotation.Changelog;
import com.kuliginstepan.mongration.annotation.Changeset;
import com.kuliginstepan.mongration.configuration.MongrationAutoConfiguration;
import com.kuliginstepan.mongration.entity.ChangesetEntity;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@DataMongoTest(properties = {
    "mongration.mode=reactive",
    "mongration.changelogsCollection=test_collection",
    "spring.data.mongodb.auto-index-creation=false",
    "logging.level.com.kuliginstepan.mongration=TRACE"
})
@EnableAutoConfiguration
@ImportAutoConfiguration(MongrationAutoConfiguration.class)
@Import({TestChangeLog.class, TestChangeLog1.class, TestChangeLog2.class})
class ReactiveMongrationTest extends MongoIntegrationTest {

    @Autowired
    private ReactiveMongoTemplate template;

    @Test
    void shouldExecuteChangeLog() {
        List<ChangesetEntity> changesets = template.findAll(ChangesetEntity.class, "test_collection").collectList().block();
        List<Document> documents = template.findAll(Document.class, "test").collectList().block();
        assertThat(template.indexOps(MongrationTest.TestDocument.class).getIndexInfo().collectList().block())
            .hasSize(2);
        assertThat(changesets)
            .hasSize(3)
            .anySatisfy(changesetEntity -> {
                assertThat(changesetEntity.getChangeset()).isEqualTo("testChangeSet");
                assertThat(changesetEntity.getChangelog()).isEqualTo("TestChangeLog");
            });
        assertThat(documents)
            .hasSize(3)
            .anySatisfy(document -> {
                assertThat(document)
                    .containsKey("_id")
                    .containsEntry("key", "value");
            });
    }

    @AfterEach
    void tearDown() {
        template.dropCollection("test_collection").block();
        template.dropCollection("test").block();
        template.dropCollection(TestDocument.class).block();
    }

    @Order(1)
    @Changelog
    public static class TestChangeLog {

        @Changeset(author = "Stepan", order = 1)
        public Mono<Void> testChangeSet(ReactiveMongoTemplate template) {
            return template.findById("LOCK", Document.class, "test_collection")
                .hasElement()
                .flatMap(hasElement -> {
                    assertThat(hasElement).isTrue();
                    return template.indexOps(MongrationTest.TestDocument.class).getIndexInfo()
                        .hasElements()
                        .flatMap(it -> {
                            assertThat(it).isFalse();
                            return template.save(new Document("key", "value"), "test").then();
                        });
                });
        }

    }

    @Order(2)
    @Changelog
    public static class TestChangeLog1 {

        @Changeset(author = "Stepan", order = 1)
        public Mono<Void> testChangeSet(ReactiveMongoTemplate template) {
            return template.findById("LOCK", Document.class, "test_collection")
                .hasElement()
                .flatMap(hasElement -> {
                    assertThat(hasElement).isTrue();
                    return template.indexOps(MongrationTest.TestDocument.class).getIndexInfo()
                        .hasElements()
                        .flatMap(it -> {
                            assertThat(it).isFalse();
                            return template.save(new Document("key", "value"), "test").then();
                        });
                });
        }

    }

    @Order(3)
    @Changelog
    public static class TestChangeLog2 {

        @Changeset(author = "Stepan", order = 1)
        public Mono<Void> testChangeSet(ReactiveMongoTemplate template) {
            return template.findById("LOCK", Document.class, "test_collection")
                .hasElement()
                .flatMap(hasElement -> {
                    assertThat(hasElement).isTrue();
                    return template.indexOps(MongrationTest.TestDocument.class).getIndexInfo()
                        .hasElements()
                        .flatMap(it -> {
                            assertThat(it).isFalse();
                            return template.save(new Document("key", "value"), "test").then();
                        });
                });
        }

    }

    @org.springframework.data.mongodb.core.mapping.Document
    public static class TestDocument {

        @Id
        private String id;
        @Indexed(unique = true)
        private String index;
    }
}
