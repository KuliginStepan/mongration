package com.kuliginstepan.mongration;

import static org.assertj.core.api.Assertions.assertThat;

import com.kuliginstepan.mongration.NewIndexMongrationTest.TestChangeLog;
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
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
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
class NewIndexMongrationTest extends MongoIntegrationTest {

    @Changelog
    public static class TestChangeLog {

        @Changeset(author = "Stepan", order = 1)
        public void testChangeSet(MongoTemplate template) {
            template.findAll(TestDocument.class)
                .forEach(doc -> {
                    doc.setIndex(doc.getId());
                    template.save(doc);
                });
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
    private MongoTemplate template;

    @BeforeAll
    static void setUp() {
        new ApplicationContextRunner()
            .withInitializer(new MongoIntegrationTest.Initializer())
            .withConfiguration(AutoConfigurations.of(MongoAutoConfiguration.class, MongoDataAutoConfiguration.class))
            .withPropertyValues("spring.data.mongodb.auto-index-creation=false")
            .run(context -> {
                var template = context.getBean(MongoTemplate.class);
                template.save(new TestDocument("1", null));
                template.save(new TestDocument("2", null));
            });
    }

    @Test
    void shouldExecuteChangeLogWithNewIndexes() {
        var changesets = template.findAll(ChangesetEntity.class, "test_collection");
        var documents = template.findAll(Document.class, "test");
        assertThat(template.indexOps(TestDocument.class).getIndexInfo()).hasSize(2);
    }

    @AfterEach
    void tearDown() {
        template.dropCollection("test_collection");
        template.dropCollection("test");
        template.dropCollection(TestDocument.class);
    }
}