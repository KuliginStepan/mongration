package com.kuliginstepan.mongration;

import static com.kuliginstepan.mongration.utils.ChangelogUtils.extractChangeset;
import static com.kuliginstepan.mongration.utils.ChangelogUtils.findChangeSetMethods;
import static com.kuliginstepan.mongration.utils.ChangelogUtils.getChangelogClass;

import com.kuliginstepan.mongration.annotation.Changelog;
import com.kuliginstepan.mongration.annotation.Changeset;
import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.service.AbstractChangeSetService;
import com.kuliginstepan.mongration.service.IndexCreator;
import com.kuliginstepan.mongration.service.LockService;
import com.kuliginstepan.mongration.utils.ChangelogUtils;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;


/**
 * Main component which executes {@link Changeset}. Execution starts when {@link ApplicationReadyEvent} is published
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMongration implements SmartInitializingSingleton {

    private final AbstractChangeSetService changesetService;
    private final IndexCreator indexCreator;
    private final LockService lockService;
    private final MongrationProperties properties;
    protected final ApplicationContext context;

    @Override
    public void afterSingletonsInstantiated() {
        log.info("mongration started");
        findMigrationsForExecution()
            .flatMap(tuple ->
                withLockAcquired(Mono.defer(() -> executeMigration(tuple)))
            )
            .block();
        log.info("mongration finished its work");
    }

    protected Mono<Void> withLockAcquired(Mono<Void> action) {
        return acquireLock()
            .then(
                action
                    .onErrorResume(throwable ->
                        lockService.releaseLock()
                            .onErrorResume(releasingException -> {
                                throwable.addSuppressed(releasingException);
                                return Mono.empty();
                            })
                            .then(Mono.error(throwable))
                    )
                    .then(Mono.defer(lockService::releaseLock))
            );
    }

    protected Mono<Void> acquireLock() {
        return Mono.defer(lockService::acquireLock)
            .retryWhen(
                Retry.fixedDelay(properties.getRetryCount(), properties.getRetryDelay())
                    .filter(e -> e instanceof MongrationException)
                    .doBeforeRetry(
                        retry -> log.warn("mongration retried {} time at {}", retry.totalRetries(), LocalTime.now()))
            );
    }

    protected abstract Mono<Object> executeChangeSetMethod(Object changelog, Method changesetMethod);

    protected List<String> doValidateChangelog(Class<?> changelogClass) {
        List<Method> changesetMethods = findChangeSetMethods(changelogClass);
        long distinctOrderCount = changesetMethods.stream()
            .map(ChangelogUtils::extractChangeset)
            .map(Changeset::order)
            .distinct()
            .count();
        long distinctIdCount = changesetMethods.stream()
            .map(method -> Optional.of(method)
                .map(ChangelogUtils::extractChangeset)
                .map(Changeset::id)
                .filter(StringUtils::hasText)
                .orElseGet(method::getName))
            .distinct()
            .count();
        List<String> errors = new ArrayList<>();
        if (distinctOrderCount != changesetMethods.size()) {
            errors.add("Several change sets have same order");
        }
        if (distinctIdCount != changesetMethods.size()) {
            errors.add("Several change sets have same id's");
        }
        return errors;
    }

    private Mono<Void> validateChangelog(Class<?> changelogClass) {
        List<String> errors = doValidateChangelog(changelogClass);
        return errors.isEmpty() ? Mono.empty() : Mono.error(() -> new MongrationException(String.join(",", errors)));
    }

    private Mono<List<Tuple2<Object, List<Method>>>> findMigrationsForExecution() {
        return Flux.fromIterable(context.getBeansWithAnnotation(Changelog.class).values())
            .flatMap(changelog ->
                validateChangelog(getChangelogClass(changelog))
                    .thenMany(Flux.fromIterable(
                        findChangeSetMethods(getChangelogClass(changelog))
                    ))
                    .collectSortedList(Comparator.comparingInt(method -> extractChangeset(method).order()))
                    .map(changesets -> Tuples.of(changelog, changesets))
            )
            .sort((x, y) -> AnnotationAwareOrderComparator.INSTANCE.compare(x.getT1(), y.getT1()))
            .collectList();
    }

    private Mono<Void> executeMigration(List<Tuple2<Object, List<Method>>> changelogs) {
        return indexCreator
            .createIndexes(
                Arrays.asList(new Index().on("changeset", Direction.ASC).on("changelog", Direction.ASC).unique()),
                properties.getChangelogsCollection())
            .log("started executing migrations")
            .thenMany(Flux.fromIterable(changelogs))
            .concatMap(this::executeChangelogMigrations)
            .then(indexCreator.createIndexes());
    }

    private Flux<Void> executeChangelogMigrations(Tuple2<Object, List<Method>> changelogTuple) {
        Object changelog = changelogTuple.getT1();

        return Flux.fromIterable(changelogTuple.getT2())
            .filterWhen(changeset -> changesetService.needExecuteChangeset(changeset, changelog))
            .concatMap(changeset -> executeMigration(changelog, changeset));
    }

    private Mono<Void> executeMigration(Object changelog, Method changesetMethod) {
        return changesetService.validateChangesetMethodSignature(changesetMethod)
            .then(Mono.defer(() -> executeChangeSetMethod(changelog, changesetMethod)))
            .then(Mono.defer(() -> changesetService.saveChangeset(changesetMethod, changelog)))
            .onErrorMap(t -> new MongrationException("Could't execute changeset: " + changesetMethod.getName(), t));
    }
}
