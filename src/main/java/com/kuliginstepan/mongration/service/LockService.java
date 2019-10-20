package com.kuliginstepan.mongration.service;

import org.bson.Document;
import reactor.core.publisher.Mono;

/**
 * Component which manages lock
 */
public interface LockService {

    Document LOCK = new Document("_id", "LOCK").append("status", "LOCK_HELD");

    /**
     * Acquires lock
     * @throws com.kuliginstepan.mongration.MongrationException
     * @return empty {@link Mono}
     */
    Mono<Void> acquireLock();

    /**
     * Releases lock
     * @throws com.kuliginstepan.mongration.MongrationException
     * @return empty {@link Mono}
     */
    Mono<Void> releaseLock();

}
