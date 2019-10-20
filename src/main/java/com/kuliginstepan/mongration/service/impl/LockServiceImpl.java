package com.kuliginstepan.mongration.service.impl;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.kuliginstepan.mongration.MongrationException;
import com.kuliginstepan.mongration.service.LockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LockServiceImpl implements LockService {

    private final String lockCollection;
    private final MongoTemplate template;

    @Override
    public Mono<Void> acquireLock() {
        try {
            template.insert(LOCK, lockCollection);
            return Mono.empty();
        } catch (Exception e) {
            return Mono.error(new MongrationException("Mongration couldn't acquire process lock", e));
        }
    }

    @Override
    public Mono<Void> releaseLock() {
        template.remove(query(where("_id").is(LOCK.get("_id"))), lockCollection);
        return Mono.empty();
    }
}
