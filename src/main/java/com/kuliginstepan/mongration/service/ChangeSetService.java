package com.kuliginstepan.mongration.service;

import static com.kuliginstepan.mongration.entity.ChangeSetEntity.CHANGE_LOG_CLASS_KEY;
import static com.kuliginstepan.mongration.entity.ChangeSetEntity.CHANGE_SET_ID_KEY;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.kuliginstepan.mongration.annotation.ChangeSet;
import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.entity.ChangeSetEntity;
import com.mongodb.client.model.IndexOptions;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
public class ChangeSetService {

    private final MongoTemplate template;
    private final MongrationProperties properties;
    private final LockService lock;

    public ChangeSetService(MongoTemplate template, MongrationProperties properties) {
        this.template = template;
        this.properties = properties;
        lock = new LockService(properties.getChangelogsCollection());
        createChangelogCollectionIfAbsent();
    }

    public static ChangeSet getChangeSet(Method changeSetMethod) {
        return changeSetMethod.getDeclaredAnnotation(ChangeSet.class);
    }

    public static Class<?> getChangelogClass(Object changelog) {
        return AopProxyUtils.ultimateTargetClass(changelog);
    }

    public void validateChangeSetMethodSignature(Method changeSetMethod) {
        Class<?>[] params = changeSetMethod.getParameterTypes();
        if (params.length == 0 || params.length > 2) {
            throw new IllegalArgumentException(
                "illegal number of arguments. 1 or 2 excepted, found " + changeSetMethod.getParameterCount());
        }
        if (params.length == 1) {
            if (!params[0].isAssignableFrom(MongoTemplate.class)) {
                throw new IllegalArgumentException("expect MongoTemplate argument");
            }
        } else {
            if (!Arrays.asList(params).containsAll(Arrays.asList(MongoTemplate.class, TransactionTemplate.class))) {
                throw new IllegalArgumentException("expect MongoTemplate and/or TransactionTemplate arguments");
            }

        }
    }

    public boolean needExecuteChangeSet(Method changeSetMethod, Object changelog) {
        ChangeSet changeSet = getChangeSet(changeSetMethod);
        return !isExistingChangeSet(changeSet, changelog) || changeSet.runAlways();
    }

    public void saveChangeSet(Method changeSetMethod, Object changelog) {
        ChangeSet changeSet = getChangeSet(changeSetMethod);
        if (changeSet.runAlways() && isExistingChangeSet(changeSet, changelog)) {
            log.info("executed 'runAlways' changeSet: {}-{}", getChangelogClass(changelog), changeSet.id());
        } else {
            ChangeSetEntity entity = new ChangeSetEntity();
            entity.setChangeSetId(changeSet.id());
            entity.setAuthor(changeSet.author());
            entity.setCreatedAt(Instant.now());
            entity.setChangeLogClass(getChangelogClass(changelog).getName());
            entity.setChangeSetMethod(changeSetMethod.getName());
            template.save(entity, properties.getChangelogsCollection());
            log.info("saved migration: {}", entity);
        }
    }

    public boolean acquireProcessLock() {
        return lock.acquireLock(template);
    }

    public void releaseProcessLock() {
        lock.releaseLock(template);
    }

    private void createChangelogCollectionIfAbsent() {
        template.getCollection(properties.getChangelogsCollection()).createIndex(new BsonDocument().append(
            CHANGE_SET_ID_KEY, new BsonString("")).append(CHANGE_LOG_CLASS_KEY, new BsonString("")),
            new IndexOptions().unique(true));
    }

    private boolean isExistingChangeSet(ChangeSet changeSet, Object changelog) {
        return template.exists(query(where(CHANGE_SET_ID_KEY).is(changeSet.id()).and(CHANGE_LOG_CLASS_KEY)
            .is(getChangelogClass(changelog).getName())), properties.getChangelogsCollection());
    }
}
