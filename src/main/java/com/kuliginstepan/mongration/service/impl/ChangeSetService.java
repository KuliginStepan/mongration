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
import org.springframework.data.mongodb.core.MongoTemplate;
import reactor.core.publisher.Mono;

public class ChangeSetService extends AbstractChangeSetService {

    private final MongoTemplate template;

    public ChangeSetService(MongrationProperties properties, MongoTemplate template) {
        super(properties);
        this.template = template;
    }

    @Override
    public Mono<Void> validateChangesetMethodSignature(Method changesetMethod) {
        return Mono.just(changesetMethod)
            .map(Method::getReturnType)
            .filter(Void.TYPE::equals)
            .switchIfEmpty(Mono.error(() -> new MongrationException("Change Set method must return 'void'")))
            .then();
    }

    @Override
    protected Mono<ChangesetEntity> saveChangesetInternal(ChangesetEntity changeset) {
        return Mono.just(template.save(changeset, properties.getChangelogsCollection()));
    }

    @Override
    protected Mono<Boolean> isExistingChangeset(Method changesetMethod, Object changelog) {
        return Mono.just(template.exists(query(where(CHANGE_SET_KEY).is(generateChangeSetId(changesetMethod))
                .and(CHANGE_LOG_KEY).is(generateChangeLogId(getChangelogClass(changelog)))),
            properties.getChangelogsCollection()));
    }
}
