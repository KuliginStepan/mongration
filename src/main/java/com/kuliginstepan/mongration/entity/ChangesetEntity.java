package com.kuliginstepan.mongration.entity;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.CompoundIndex;

@Data
@CompoundIndex(def = "{'changeset': 1, 'changelog': 1}", unique = true)
public class ChangesetEntity {

    public static final String CHANGE_SET_KEY = "changeset";
    public static final String CHANGE_LOG_KEY = "changelog";

    @Id
    private String id;
    private final String changeset;
    private final String author;
    private final Instant createdAt;
    private final String changelog;

    @Builder
    public ChangesetEntity(String changeset, String author, String changelog) {
        this.changeset = changeset;
        this.author = author;
        this.changelog = changelog;
        createdAt = Instant.now();
    }

    @PersistenceCreator
    public ChangesetEntity(String id, String changeset, String author, Instant createdAt, String changelog) {
        this.id = id;
        this.changeset = changeset;
        this.author = author;
        this.createdAt = createdAt;
        this.changelog = changelog;
    }
}
