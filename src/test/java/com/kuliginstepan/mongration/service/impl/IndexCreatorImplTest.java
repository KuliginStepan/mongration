package com.kuliginstepan.mongration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kuliginstepan.mongration.MongoIntegrationTest;
import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.entity.ChangesetEntity;
import com.kuliginstepan.mongration.service.IndexCreator;
import com.kuliginstepan.mongration.service.impl.IndexCreatorImplTest.TestConfig;
import com.kuliginstepan.mongration.service.impl.IndexCreatorImplTest.TestDocument;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperationsProvider;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataMongoTest(properties = {
    "mongration.mode=imperative",
    "mongration.changelogsCollection=test_collection",
    "spring.data.mongodb.autoIndexCreation=false"
})
@EnableAutoConfiguration
@EntityScan(basePackageClasses = {ChangesetEntity.class, TestDocument.class})
@Import(TestConfig.class)
class IndexCreatorImplTest extends MongoIntegrationTest {

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public IndexCreator indexCreator(MappingContext mappingContext, IndexOperationsProvider provider) {
            return new IndexCreatorImpl(mappingContext, provider);
        }

        @Bean
        public MongrationProperties mongrationProperties() {
            return new MongrationProperties();
        }

    }

    @Autowired
    private IndexCreator indexCreator;
    @Autowired
    private MongoTemplate template;

    @Test
    void shouldCreateIndexForClass() {
        indexCreator.createIndexes(TestDocument.class, "testDocument").block();
        Spliterator<Document> spliterator = template.getCollection("testDocument").listIndexes().spliterator();
        List<Document> indexes = StreamSupport.stream(spliterator, false).collect(Collectors.toList());
        assertThat(indexes)
            .hasSize(2)
            .anySatisfy(index -> {
                assertThat(index).hasEntrySatisfying("key", key -> {
                    assertThat((Document) key)
                        .containsEntry("index", 1);
                });
            });
    }

    @Test
    void shouldCreateIndex() {
        indexCreator.createIndexes().block();
        Spliterator<Document> spliterator = template.getCollection("testDocument").listIndexes().spliterator();
        List<Document> indexes = StreamSupport.stream(spliterator, false).collect(Collectors.toList());
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
        template.dropCollection("test_collection");
        template.dropCollection("testDocument");
    }

    @org.springframework.data.mongodb.core.mapping.Document
    public static class TestDocument {

        @Id
        private String id;
        @Indexed(unique = true)
        private String index;
    }
}