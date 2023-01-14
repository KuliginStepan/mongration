package com.kuliginstepan.mongration.concurrent;

import static com.kuliginstepan.mongration.concurrent.ConcurrentMongrationTest.HighlyScaledApp.APP_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import com.kuliginstepan.mongration.MongoIntegrationTest;
import com.kuliginstepan.mongration.ReactiveMongration;
import com.kuliginstepan.mongration.annotation.Changelog;
import com.kuliginstepan.mongration.annotation.Changeset;
import com.kuliginstepan.mongration.configuration.MongrationProperties;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@ExtendWith(SpringExtension.class)
@DataMongoTest
@EnableAutoConfiguration(exclude = ConcurrentMongrationTest.HighlyScaledApp.class)
class ConcurrentMongrationTest extends MongoIntegrationTest {

    public static final String NODE_1 = "node-1";
    public static final String NODE_2 = "node-2";
    public static final String NODE_3 = "node-3";
    public static final Map<String, NodeContext> nodesMap = Map.of(
        NODE_1, new NodeContext(NODE_1),
        NODE_2, new NodeContext(NODE_2),
        NODE_3, new NodeContext(NODE_3)
    );
    private static final String CHANGELOG_COLLECTION = "changelogs";
    private static final int RETRY_COUNT = 12;

    @Autowired
    private ReactiveMongoTemplate template;

    @AfterEach
    void tearDown() {
        template.dropCollection(CHANGELOG_COLLECTION).block();
        template.dropCollection(TestAppInfo.class).block();
    }

    @Test
    void shouldGuardMongrationProcessWithLock() {
        try {
            Mono.zip(
                runNode(NODE_1),
                runNode(NODE_2),
                runNode(NODE_3)
            )
                .subscribe(tuple ->
                    log.info("Finished running 3 app nodes: {}", tuple)
                );

            NodeContext node1 = nodesMap.get(NODE_1);
            NodeContext node2 = nodesMap.get(NODE_2);
            NodeContext node3 = nodesMap.get(NODE_3);
            nodesMap.forEach((node, ctx) -> log.info("{}: {}", node, ctx));

            // wait nodes to be ready to mongrate
            await(node1.mongrationIsReadyToStart);
            await(node2.mongrationIsReadyToStart);
            await(node3.mongrationIsReadyToStart);

            // execute node 1 changeset and suspend
            node1.shouldPerformMongration.countDown();
            await(node1.executionPerformed);

            // allow node 2 mongration
            node2.shouldPerformMongration.countDown();
            node2.shouldFinishExecution.countDown();

            // ensure node 2 waits node 1 to finish
            await(node2.mongrationFinished, false, 5);

            // finish node 1 changeset to unblock node 2
            node1.shouldFinishExecution.countDown();

            // ensure node 2 skipped mongration since no changes
            await(node2.mongrationFinished);
            await(node2.executionPerformed, false, 1);

            // allow node 3 mongration
            node3.shouldPerformMongration.countDown();
            node3.shouldFinishExecution.countDown();

            // ensure node 3 skipped mongration since no changes
            await(node3.mongrationFinished);
            await(node3.executionPerformed, false, 1);

            // verify only node 1 changeset is executed
            assertThat(template.findAll(TestAppInfo.class).collectList().block())
                .hasSize(1)
                .hasSameElementsAs(List.of(new TestAppInfo(APP_NAME, "node-1", 1)));
        } catch (IllegalStateException e) {
            if (e.getMessage().startsWith("Latch should")) {
                nodesMap.forEach((node, ctx) -> {
                    if (ctx.throwableRef.get() != null) {
                        IllegalStateException wrappedException = new IllegalStateException(String.format(
                            "App node `%s` finished with error!", node
                        ), ctx.throwableRef.get());
                        wrappedException.addSuppressed(e);
                        throw wrappedException;
                    }
                });
            }
            throw e;
        }
    }

    private Mono<Optional<ConfigurableApplicationContext>> runNode(String node) {
        log.info("Starting app node: {}", node);
        return Mono.fromCallable(() ->
            new SpringApplicationBuilder(HighlyScaledApp.class)
                .initializers(new Initializer())
                .properties(
                    "mongration.mode=reactive",
                    "mongration.changelogsCollection=" + CHANGELOG_COLLECTION,
                    "mongration.retryDelay=1s",
                    "mongration.retryCount=" + RETRY_COUNT,
                    "spring.data.mongodb.auto-index-creation=false",
                    "server.port=0",
                    "node=" + node,
                    "logging.level.com.kuliginstepan.mongration=TRACE"
                )
                .profiles(node)
                .run()
            )
            .subscribeOn(Schedulers.boundedElastic())
            .map(context -> {
                log.info("App node `{}` finished successfully.", node);
                return Optional.of(context);
            })
            .onErrorResume(throwable -> {
                log.error("App node `{}` finished with error!", node, throwable);
                nodesMap.get(node).throwableRef.set(throwable);
                return Mono.just(Optional.empty());
            });
    }

    static void await(CountDownLatch latch) {
        await(latch, true, RETRY_COUNT);
    }

    static void await(CountDownLatch latch, boolean shouldPass, long seconds) {
        log.info("Awaiting {} to {}pass in {} seconds", latch, shouldPass ? "" : "not ", seconds);
        try {
            if (latch.await(seconds, TimeUnit.SECONDS) != shouldPass) {
                throw new IllegalStateException(String.format(
                    "Latch should%s pass in %s seconds: %s", shouldPass ? "" : "n't", seconds, latch
                ));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted during waiting for a permission to execute changeset!", e);
        }
    }

    @TestConfiguration
    @SpringBootApplication
    public static class HighlyScaledApp {

        public static final String APP_NAME = "highly_scaled_app";
    }

    @Component
    public static class SpyingReactiveMongration extends ReactiveMongration {

        @Value("${node}")
        private String node;

        public SpyingReactiveMongration(MongrationProperties properties,
                                        ReactiveMongoTemplate mongoTemplate,
                                        ApplicationContext context) {
            super(properties, mongoTemplate, context);
        }

        @Override
        public void afterSingletonsInstantiated() {
            NodeContext nodeContext = nodesMap.get(node);
            nodeContext.mongrationIsReadyToStart.countDown();
            await(nodeContext.shouldPerformMongration);

            super.afterSingletonsInstantiated();
            nodeContext.mongrationFinished.countDown();
        }

    }

    @Changelog
    public static class TestChangeLog {

        @Value("${node}")
        private String node;

        @Changeset(author = "Evgenii", order = 1)
        public Mono<Void> maliciousNonIdempotentChangeSet(ReactiveMongoTemplate template) {
            log.info("Executing changeset");
            NodeContext nodeContext = nodesMap.get(node);
            return template.upsert(
                query(where("name").is(APP_NAME)),
                update("masterNode", node),
                TestAppInfo.class
            )
                .doOnSuccess(updateResult -> {
                    nodeContext.executionPerformed.countDown();
                    await(nodeContext.shouldFinishExecution);
                })
                .doFinally(signalType -> {
                    log.info("Executing changeset: done");
                })
                .then();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Document
    public static class TestAppInfo {

        @Indexed(unique = true)
        private String name;
        private String masterNode;
        @Version
        private long version;
    }

    @Data
    public static class NodeContext {

        private final String node;
        private final CountDownLatch mongrationIsReadyToStart = new CountDownLatch(1);
        private final CountDownLatch shouldPerformMongration = new CountDownLatch(1);
        private final CountDownLatch executionPerformed = new CountDownLatch(1);
        private final CountDownLatch shouldFinishExecution = new CountDownLatch(1);
        private final CountDownLatch mongrationFinished = new CountDownLatch(1);
        private final AtomicReference<Throwable> throwableRef = new AtomicReference<>();
    }
}
