package com.kuliginstepan.mongration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kuliginstepan.mongration.MongoIntegrationTest;
import com.kuliginstepan.mongration.annotation.Changelog;
import com.kuliginstepan.mongration.annotation.Changeset;
import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.entity.ChangesetEntity;
import com.kuliginstepan.mongration.service.impl.ChangesetServiceIntegrationTest.TestConfig;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ReflectionUtils;

@ExtendWith(SpringExtension.class)
@DataMongoTest(properties = {
    "mongration.mode=imperative",
    "mongration.changelogsCollection=test_collection"
})
@EnableAutoConfiguration
@Import({ChangeSetService.class, TestConfig.class})
class ChangesetServiceIntegrationTest extends MongoIntegrationTest {

    private static final Method CHANGE_SET = ReflectionUtils.findMethod(TestChangeLog.class, "testChangeSet");
    private static final Object CHANGE_LOG = new TestChangeLog();

    @TestConfiguration
    static class TestConfig {

        @Bean
        public MongrationProperties mongrationProperties() {
            return new MongrationProperties();
        }
    }

    @Autowired
    private MongoTemplate template;
    @Autowired
    private ChangeSetService service;

    @Test
    void shouldSaveChangeSet() {
        service.saveChangeset(CHANGE_SET, CHANGE_LOG).block();
        var entities = template.findAll(ChangesetEntity.class);
        assertThat(entities)
            .hasSize(1)
            .anySatisfy(entity -> {
                assertThat(entity.getChangelog()).isEqualTo("testChangeLog");
                assertThat(entity.getChangeset()).isEqualTo("testChangeSet");
                assertThat(entity.getAuthor()).isEqualTo("Stepan");
            });
    }

    @Test
    void shouldFindExistingChangeSet() {
        service.saveChangeset(CHANGE_SET, CHANGE_LOG).block();
        var isExistingChangeSet = service.isExistingChangeset(CHANGE_SET, CHANGE_LOG).block();
        var needExecuteChangeSet = service.needExecuteChangeset(CHANGE_SET, CHANGE_LOG).block();

        assertThat(isExistingChangeSet).isTrue();
        assertThat(needExecuteChangeSet).isFalse();

    }

    @Test
    void shouldNotFindNonExistingChangeSet() {
        var isExistingChangeSet = service.isExistingChangeset(CHANGE_SET, CHANGE_LOG).block();
        var needExecuteChangeSet = service.needExecuteChangeset(CHANGE_SET, CHANGE_LOG).block();
        assertThat(isExistingChangeSet).isFalse();
        assertThat(needExecuteChangeSet).isTrue();
    }

    @AfterEach
    void tearDown() {
        template.dropCollection("test_collection");
    }

    @Changelog("testChangeLog")
    public static class TestChangeLog {

        @Changeset(order = 1, author = "Stepan")
        private void testChangeSet() {

        }
    }
}