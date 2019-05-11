package com.kuliginstepan.mongration.entity;

import java.time.Instant;
import lombok.Data;

/**
 * Entity used to save executed {@link com.kuliginstepan.mongration.annotation.ChangeSet} in collection specified with
 * {@link org.springframework.boot.autoconfigure.mongo.MongoProperties}
 */
@Data
public class ChangeSetEntity {

    public static final String CHANGE_SET_ID_KEY = "changeSetId";
    public static final String CHANGE_LOG_CLASS_KEY = "changeLogClass";

    private String id;
    private String changeSetId;
    private String author;
    private Instant createdAt;
    private String changeLogClass;
    private String changeSetMethod;
}
