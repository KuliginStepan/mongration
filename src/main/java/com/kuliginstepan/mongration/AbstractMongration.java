package com.kuliginstepan.mongration;

import static com.kuliginstepan.mongration.utils.ChangelogUtils.extractChangeset;
import static com.kuliginstepan.mongration.utils.ChangelogUtils.findChangeSetMethods;
import static com.kuliginstepan.mongration.utils.ChangelogUtils.getChangelogClass;

import com.kuliginstepan.mongration.annotation.Changelog;
import com.kuliginstepan.mongration.annotation.Changeset;
import com.kuliginstepan.mongration.entity.ChangesetEntity;
import com.kuliginstepan.mongration.service.AbstractChangeSetService;
import com.kuliginstepan.mongration.service.IndexCreator;
import com.kuliginstepan.mongration.service.LockService;
import com.kuliginstepan.mongration.utils.ChangelogUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;


/**
 * Main component which executes {@link Changeset}. Execution starts when {@link ApplicationReadyEvent} will be
 * published
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMongration {

    private final AbstractChangeSetService changesetService;
    private final IndexCreator indexCreator;
    private final LockService lockService;
    protected ApplicationContext context;

    @EventListener
    public void start(ApplicationReadyEvent event) {
        context = event.getApplicationContext();
        log.info("mongration started");
        findMigrationsForExecution()
            .publishOn(Schedulers.immediate())
            .flatMap(tuple -> lockService.acquireLock()
                .then(Mono.defer(() -> indexCreator.createIndexes(ChangesetEntity.class)))
                .then(Mono.defer(() -> {
                    return Flux.fromIterable(tuple)
                        .flatMap(this::executeChangelogMigrations)
                        .then();
                }))
//                .then(Mono.defer(() -> executeChangelogMigrations(tuple)))
                .then(Mono.defer(indexCreator::createIndexes))
                .then(Mono.defer(lockService::releaseLock))
                .onErrorResume(t -> lockService.releaseLock()
                    .then(Mono.error(t))))
            .then()
            .block();
        log.info("mongration finished his work");
    }

    protected abstract Mono<Object> executeChangeSetMethod(Object changelog, Method changesetMethod);

    protected List<String> doValidateChangelog(Class<?> changelogClass) {
        var changesetMethods = findChangeSetMethods(changelogClass);
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
        var errors = new ArrayList<String>();
        if (distinctOrderCount != changesetMethods.size()) {
            errors.add("Several change sets have same order");
        }
        if (distinctIdCount != changesetMethods.size()) {
            errors.add("Several change sets have same id's");
        }
        return errors;
    }

    private Mono<Void> validateChangelog(Class<?> changelogClass) {
        var errors = doValidateChangelog(changelogClass);
        return errors.isEmpty() ? Mono.empty() : Mono.error(() -> new MongrationException(String.join(",", errors)));
    }

    private Mono<List<Tuple2<Object, List<Method>>>> findMigrationsForExecution() {
        return Flux.fromIterable(context.getBeansWithAnnotation(Changelog.class).values())
            .flatMap(changelog -> validateChangelog(getChangelogClass(changelog))
                .thenMany(Flux.fromIterable(findChangeSetMethods(getChangelogClass(changelog))))
                .filterWhen(method -> changesetService.needExecuteChangeset(method, changelog))
                .collectSortedList(Comparator.comparingInt(method -> extractChangeset(method).order()))
                .map(changesets -> Tuples.of(changelog, changesets)))
            .sort(AnnotationAwareOrderComparator.INSTANCE)
            .collectList();
    }

    private Mono<Void> executeChangelogMigrations(Tuple2<Object, List<Method>> tuple) {
        log.info("started executing migrations");
        return Flux.fromIterable(tuple.getT2())
            .flatMap(changeset -> executeMigration(tuple.getT1(), changeset))
            .then();
    }

    private Mono<Void> executeMigration(Object changelog, Method changesetMethod) {
        return changesetService.validateChangesetMethodSignature(changesetMethod)
            .then(Mono.defer(() -> executeChangeSetMethod(changelog, changesetMethod)))
            .then(Mono.defer(() -> changesetService.saveChangeset(changesetMethod, changelog)))
            .onErrorMap(t -> new MongrationException("Could't execute changeset: " + changesetMethod.getName(), t));
    }

}
