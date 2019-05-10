package com.kuliginstepan.mongration.service;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author colsson11
 * @since 13.01.15
 */
@Slf4j
public class LockService {

    private static final String KEY_PROP_NAME = "_id";

    private static final String LOCK_ENTRY_KEY_VAL = "LOCK";
    private String lockCollectionName;

    public LockService(String lockCollectionName) {
        this.lockCollectionName = lockCollectionName;
    }

    public boolean acquireLock(MongoTemplate template) {

        Document insertObj = new Document(KEY_PROP_NAME, LOCK_ENTRY_KEY_VAL).append("status", "LOCK_HELD");

        try {
            template.insert(insertObj, lockCollectionName);
        } catch (Exception ex) {
            log.warn("Exception while acquireLock. Probably the lock has been already acquired.");
            return false;
        }
        return true;
    }

    public void releaseLock(MongoTemplate template) {
        template.remove(query(where(KEY_PROP_NAME).is(LOCK_ENTRY_KEY_VAL)), lockCollectionName);
    }

}
