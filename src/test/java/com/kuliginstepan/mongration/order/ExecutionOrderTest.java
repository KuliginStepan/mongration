package com.kuliginstepan.mongration.order;

import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.assertThat;

import com.kuliginstepan.mongration.MongoIntegrationTest;
import com.kuliginstepan.mongration.annotation.Changelog;
import com.kuliginstepan.mongration.annotation.Changeset;
import com.kuliginstepan.mongration.order.ExecutionOrderTest.Changelog1;
import com.kuliginstepan.mongration.order.ExecutionOrderTest.Changelog2;
import com.kuliginstepan.mongration.order.ExecutionOrderTest.SampleApplication;
import java.util.concurrent.ArrayBlockingQueue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@SpringBootTest(classes = {
    SampleApplication.class,
    Changelog1.class,
    Changelog2.class
}, properties = {
    "spring.data.mongodb.auto-index-creation=false",
    "logging.level.com.kuliginstepan.mongration=TRACE"
})
public class ExecutionOrderTest extends MongoIntegrationTest {

    private static final ArrayBlockingQueue<String> changesets = new ArrayBlockingQueue<>(8);

    @Test
    void shouldExecuteSequentiallyInSingleThread() {
        assertThat(changesets)
            .hasSize(8)
            .containsExactly(
                "changeset1:start",
                "changeset1:end",
                "changeset2:start",
                "changeset2:end",
                "changeset3:start",
                "changeset3:end",
                "changeset4:start",
                "changeset4:end"
            );
    }

    @Order(2)
    @Changelog
    public static class Changelog2 {

        @Transactional
        @Changeset(author = "Evgenii", order = 4)
        public Mono<Void> changeset4(ReactiveMongoTemplate template) {
            changesets.add("changeset4:start");
            return Mono.delay(ofMillis(400))
                .then(Mono.defer(() -> {
                    changesets.add("changeset4:end");
                    return Mono.empty();
                }));
        }

        @Transactional
        @Changeset(author = "Evgenii", order = 3)
        public Mono<Void> changeset3(ReactiveMongoTemplate template) {
            changesets.add("changeset3:start");
            return Mono.delay(ofMillis(600))
                .then(Mono.defer(() -> {
                    changesets.add("changeset3:end");
                    return Mono.empty();
                }));
        }
    }

    @Order(1)
    @Changelog
    public static class Changelog1 {

        @Transactional
        @Changeset(author = "Evgenii", order = 1)
        public Mono<Void> changeset1(ReactiveMongoTemplate template) {
            changesets.add("changeset1:start");
            return Mono.delay(ofMillis(1000))
                .then(Mono.defer(() -> {
                    changesets.add("changeset1:end");
                    return Mono.empty();
                }));
        }

        @Transactional
        @Changeset(author = "Evgenii", order = 2)
        public Mono<Void> changeset2(ReactiveMongoTemplate template) {
            changesets.add("changeset2:start");
            return Mono.delay(ofMillis(800))
                .then(Mono.defer(() -> {
                    changesets.add("changeset2:end");
                    return Mono.empty();
                }));
        }
    }

    @TestConfiguration
    @SpringBootApplication
    public static class SampleApplication {

        public static void main(String[] args) {
            SpringApplication.run(SampleApplication.class, args);
        }
    }
}
