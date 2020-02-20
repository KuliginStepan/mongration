package com.kuliginstepan.mongration;

import static org.assertj.core.api.Assertions.assertThat;

import com.kuliginstepan.mongration.MongrationTest.TestChangeLog;
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
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataMongoTest(properties = {
    "mongration.mode=imperative",
    "mongration.changelogsCollection=test_collection",
    "spring.data.mongodb.auto-index-creation=false"
})
@EnableAutoConfiguration
@ImportAutoConfiguration(MongrationAutoConfiguration.class)
@Import(TestChangeLog.class)
class MongrationTest extends MongoIntegrationTest {

    @Changelog
    public static class TestChangeLog {

        @Changeset(author = "Stepan", order = 1)
        public void testChangeSet(MongoTemplate template) {
            assertThat(template.findById("LOCK", Document.class, "test_collection")).isNotNull();
            assertThat(template.indexOps(TestDocument.class).getIndexInfo()).hasSize(0);
            template.save(new Document("key", "value"), "test");
        }

    }

    @org.springframework.data.mongodb.core.mapping.Document
    public static class TestDocument {

        @Id
        private String id;
        @Indexed(unique = true)
        private String index;
    }

    @Autowired
    private MongoTemplate template;

    @Test
    void shouldExecuteChangeLog() {
        List<ChangesetEntity> changesets = template.findAll(ChangesetEntity.class, "test_collection");
        List<Document> documents = template.findAll(Document.class, "test");
        assertThat(template.indexOps(TestDocument.class).getIndexInfo()).hasSize(2);
        assertThat(changesets)
            .hasSize(1)
            .anySatisfy(changesetEntity -> {
                assertThat(changesetEntity.getChangeset()).isEqualTo("testChangeSet");
                assertThat(changesetEntity.getChangelog()).isEqualTo("TestChangeLog");
            });
        assertThat(documents)
            .hasSize(1)
            .anySatisfy(document -> {
                assertThat(document)
                    .containsKey("_id")
                    .containsEntry("key", "value");
            });
    }

    @AfterEach
    void tearDown() {
        template.dropCollection("test_collection");
        template.dropCollection("test");
        template.dropCollection(TestDocument.class);
    }
}