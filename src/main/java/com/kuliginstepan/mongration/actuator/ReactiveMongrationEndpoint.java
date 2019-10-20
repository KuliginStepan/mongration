package com.kuliginstepan.mongration.actuator;

import com.kuliginstepan.mongration.entity.ChangesetEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

@Endpoint(id = "mongration")
@RequiredArgsConstructor
public class ReactiveMongrationEndpoint {

    private final ReactiveMongoTemplate template;

    @ReadOperation
    public Mono<List<ChangesetEntity>> getExecutedChangesets() {
        return template.findAll(ChangesetEntity.class).collectList();
    }
}
