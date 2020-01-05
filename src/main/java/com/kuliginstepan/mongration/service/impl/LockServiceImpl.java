package com.kuliginstepan.mongration.service.impl;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.kuliginstepan.mongration.MongrationException;
import com.kuliginstepan.mongration.service.LockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class LockServiceImpl implements LockService {

    private final String lockCollection;
    private final MongoTemplate template;

    @Override
    public Mono<Void> acquireLock() {
        try {
            template.insert(LOCK, lockCollection);
            log.trace("Mongration acquired process lock");
            return Mono.empty();
        } catch (Exception e) {
            return Mono.error(new MongrationException("Mongration couldn't acquire process lock", e));
        }
    }

    @Override
    public Mono<Void> releaseLock() {
        try {
            template.remove(query(where("_id").is(LOCK.get("_id"))), lockCollection);
            log.trace("Mongration released process lock");
            return Mono.empty();
        } catch (Exception e) {
            return Mono.error(new MongrationException("Mongration couldn't release process lock", e));
        }
    }
}
