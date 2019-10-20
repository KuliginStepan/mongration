package com.kuliginstepan.mongration.service;

import static com.kuliginstepan.mongration.utils.ChangelogUtils.extractChangeset;
import static com.kuliginstepan.mongration.utils.ChangelogUtils.getChangelogClass;

import com.kuliginstepan.mongration.annotation.Changeset;
import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.entity.ChangesetEntity;
import com.kuliginstepan.mongration.utils.ChangelogUtils;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Component which can manage changesets:
 * <ul>
 * <li>validates changesets
 * <li>chooses to execute changeset or not
 * <li>saves changeset
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractChangeSetService {

    protected final MongrationProperties properties;

    public abstract Mono<Void> validateChangesetMethodSignature(Method changesetMethod);

    public Mono<Boolean> needExecuteChangeset(Method changesetMethod, Object changelog) {
        return isExistingChangeset(changesetMethod, changelog)
            .map(isExisting -> !isExisting);
    }

    public Mono<Void> saveChangeset(Method changesetMethod, Object changelog) {
        Changeset changeset = extractChangeset(changesetMethod);
        return saveChangesetInternal(toEntity(changeset, getChangelogClass(changelog), changesetMethod))
            .doOnSuccess(entity -> log.info("saved migration: {}", entity))
            .then();
    }

    protected abstract Mono<ChangesetEntity> saveChangesetInternal(ChangesetEntity changeset);

    protected abstract Mono<Boolean> isExistingChangeset(Method changesetMethod, Object changelog);

    private static ChangesetEntity toEntity(Changeset changeset, Class<?> changelogClass, Method changesetMethod) {
        return ChangesetEntity.builder()
            .changeset(ChangelogUtils.generateChangeSetId(changesetMethod))
            .author(changeset.author())
            .changelog(ChangelogUtils.generateChangeLogId(changelogClass))
            .build();
    }
}
