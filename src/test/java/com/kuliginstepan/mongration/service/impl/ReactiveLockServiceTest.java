package com.kuliginstepan.mongration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kuliginstepan.mongration.MongoIntegrationTest;
import com.kuliginstepan.mongration.MongrationException;
import com.kuliginstepan.mongration.configuration.MongrationAutoConfiguration;
import com.kuliginstepan.mongration.service.LockService;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataMongoTest
@EnableAutoConfiguration(exclude = MongrationAutoConfiguration.class)
class ReactiveLockServiceTest extends MongoIntegrationTest {

    private static final String COLLECTION_NAME = "test_collection";
    @Autowired
    private ReactiveMongoTemplate template;
    private LockService service;

    @BeforeEach
    void setUp() {
        service = new ReactiveLockServiceImpl(COLLECTION_NAME, template);
    }

    @Test
    void shouldAcquireLock() {
        service.acquireLock().block();
        var documents = template.findAll(Document.class, COLLECTION_NAME).collectList().block();
        assertThat(documents)
            .hasSize(1)
            .anySatisfy(lock -> {
                assertThat(lock.get("_id", String.class)).isEqualTo("LOCK");
                assertThat(lock.get("status", String.class)).isEqualTo("LOCK_HELD");
            });
    }

    @Test
    void shouldReleaseLock() {
        service.acquireLock().block();
        service.releaseLock().block();
        var documents = template.findAll(Document.class, COLLECTION_NAME).collectList().block();
        assertThat(documents).isEmpty();
    }

    @Test
    void shouldNotAcquireLockIfLockHasBeenAlreadyAcquired() {
        service.acquireLock().block();
        assertThrows(MongrationException.class, () -> service.acquireLock().block());
    }

    @AfterEach
    void tearDown() {
        template.dropCollection(COLLECTION_NAME).block();
    }
}