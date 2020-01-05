package com.kuliginstepan.mongration.service.impl;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.kuliginstepan.mongration.MongrationException;
import com.kuliginstepan.mongration.service.LockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class ReactiveLockServiceImpl implements LockService {

    private final String lockCollection;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Void> acquireLock() {
        return template.insert(LOCK, lockCollection)
            .onErrorMap(t -> new MongrationException("Mongration couldn't acquire process lock", t))
            .then(Mono.defer(() -> {
                log.trace("Mongration acquired process lock");
                return Mono.empty();
            }));
    }

    @Override
    public Mono<Void> releaseLock() {
        return template.remove(query(where("_id").is(LOCK.get("_id"))), lockCollection)
            .onErrorMap(t -> new MongrationException("Mongration couldn't release process lock", t))
            .then(Mono.defer(() -> {
                log.trace("Mongration released process lock");
                return Mono.empty();
            }));
    }
}
