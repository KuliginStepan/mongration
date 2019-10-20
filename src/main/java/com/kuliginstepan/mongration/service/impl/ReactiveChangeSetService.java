package com.kuliginstepan.mongration.service.impl;

import static com.kuliginstepan.mongration.entity.ChangesetEntity.CHANGE_LOG_KEY;
import static com.kuliginstepan.mongration.entity.ChangesetEntity.CHANGE_SET_KEY;
import static com.kuliginstepan.mongration.utils.ChangelogUtils.generateChangeLogId;
import static com.kuliginstepan.mongration.utils.ChangelogUtils.generateChangeSetId;
import static com.kuliginstepan.mongration.utils.ChangelogUtils.getChangelogClass;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.kuliginstepan.mongration.MongrationException;
import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.entity.ChangesetEntity;
import com.kuliginstepan.mongration.service.AbstractChangeSetService;
import java.lang.reflect.Method;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

public class ReactiveChangeSetService extends AbstractChangeSetService {

    private final ReactiveMongoTemplate template;

    public ReactiveChangeSetService(MongrationProperties properties, ReactiveMongoTemplate template) {
        super(properties);
        this.template = template;
    }

    @Override
    public Mono<Void> validateChangesetMethodSignature(Method changesetMethod) {
        return Mono.just(changesetMethod)
            .map(Method::getReturnType)
            .filter(Mono.class::isAssignableFrom)
            .switchIfEmpty(Mono.error(() -> new MongrationException("Change Set method must return 'Mono'")))
            .then();
    }

    @Override
    protected Mono<ChangesetEntity> saveChangesetInternal(ChangesetEntity changeset) {
        return template.save(changeset, properties.getChangelogsCollection());
    }

    @Override
    protected Mono<Boolean> isExistingChangeset(Method changesetMethod, Object changelog) {
        return template.exists(query(where(CHANGE_SET_KEY).is(generateChangeSetId(changesetMethod))
                .and(CHANGE_LOG_KEY).is(generateChangeLogId(getChangelogClass(changelog)))),
            properties.getChangelogsCollection());
    }
}
