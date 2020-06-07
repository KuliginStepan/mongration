package com.kuliginstepan.mongration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kuliginstepan.mongration.MongoIntegrationTest;
import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.entity.ChangesetEntity;
import com.kuliginstepan.mongration.service.IndexCreator;
import com.kuliginstepan.mongration.service.impl.IndexCreatorImplTest.TestDocument;
import com.kuliginstepan.mongration.service.impl.ReactiveIndexCreatorImplTest.TestConfig;
import com.mongodb.reactivestreams.client.MongoCollection;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataMongoTest(properties = {
    "mongration.mode=reactive",
    "mongration.changelogsCollection=test_collection",
    "spring.data.mongodb.autoIndexCreation=false"
})
@EntityScan(basePackageClasses = {ChangesetEntity.class, TestDocument.class})
@EnableAutoConfiguration
@Import(TestConfig.class)
class ReactiveIndexCreatorImplTest extends MongoIntegrationTest {

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public IndexCreator indexCreator(MappingContext mappingContext, ReactiveMongoTemplate provider) {
            return new ReactiveIndexCreatorImpl(mappingContext, provider::indexOps);
        }

        @Bean
        public MongrationProperties mongrationProperties() {
            return new MongrationProperties();
        }

    }

    @Autowired
    private IndexCreator indexCreator;
    @Autowired
    private ReactiveMongoTemplate template;

    @Test
    void shouldCreateIndexForClass() {
        indexCreator.createIndexes(ChangesetEntity.class).block();
        template.getCollection("test_collection")
            .flatMapMany(MongoCollection::listIndexes)
            .collectList()
            .block();
        List<Document> indexes = template.getCollection("test_collection")
            .flatMapMany(MongoCollection::listIndexes)
            .collectList()
            .block();
        assertThat(indexes)
            .hasSize(2)
            .anySatisfy(index -> {
                assertThat(index).hasEntrySatisfying("key", key -> {
                    assertThat((Document) key)
                        .containsEntry("changeset", 1)
                        .containsEntry("changelog", 1);
                });
            });
    }

    @Test
    void shouldCreateIndex() {
        indexCreator.createIndexes().block();
        List<Document> indexes = template.getCollection("test_collection")
            .flatMapMany(MongoCollection::listIndexes)
            .collectList()
            .block();
        assertThat(indexes)
            .hasSize(2)
            .anySatisfy(index -> {
                assertThat(index).hasEntrySatisfying("key", key -> {
                    assertThat((Document) key)
                        .containsEntry("changeset", 1)
                        .containsEntry("changelog", 1);
                });
            });
        indexes = template.getCollection("testDocument")
            .flatMapMany(MongoCollection::listIndexes)
            .collectList()
            .block();
        assertThat(indexes)
            .hasSize(2)
            .anySatisfy(index -> {
                assertThat(index).hasEntrySatisfying("key", key -> {
                    assertThat((Document) key)
                        .containsEntry("index", 1);
                });
            });
    }

    @AfterEach
    void tearDown() {
        template.dropCollection("test_collection").block();
        template.dropCollection("testDocument").block();
    }

    @org.springframework.data.mongodb.core.mapping.Document
    public static class TestDocument {

        @Id
        private String id;
        @Indexed(unique = true)
        private String index;
    }
}