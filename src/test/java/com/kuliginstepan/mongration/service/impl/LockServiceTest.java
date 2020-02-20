package com.kuliginstepan.mongration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import com.kuliginstepan.mongration.MongoIntegrationTest;
import com.kuliginstepan.mongration.MongrationException;
import com.kuliginstepan.mongration.configuration.MongrationAutoConfiguration;
import com.kuliginstepan.mongration.service.LockService;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataMongoTest
@EnableAutoConfiguration(exclude = MongrationAutoConfiguration.class)
class LockServiceTest extends MongoIntegrationTest {

    private static final String COLLECTION_NAME = "test_collection";

    @SpyBean
    private MongoTemplate template;
    private LockService service;

    @BeforeEach
    void setUp() {
        service = new LockServiceImpl(COLLECTION_NAME, template);
    }

    @Test
    void shouldAcquireLock() {
        service.acquireLock().block();
        List<Document> documents = template.findAll(Document.class, COLLECTION_NAME);
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
        List<Document> documents = template.findAll(Document.class, COLLECTION_NAME);
        assertThat(documents).isEmpty();
    }

    @Test
    void shouldNotAcquireLockIfLockHasBeenAlreadyAcquired() {
        service.acquireLock().block();
        assertThrows(MongrationException.class, () -> service.acquireLock().block());
    }

    @Test
    void shouldWrapExceptionsIfLockReleasingFailed() {
        doThrow(new RuntimeException("Sample error!"))
            .when(template).remove(any(Query.class), anyString());

        assertThrows(MongrationException.class, () -> service.releaseLock().block());
    }

    @AfterEach
    void tearDown() {
        template.dropCollection(COLLECTION_NAME);
    }
}
