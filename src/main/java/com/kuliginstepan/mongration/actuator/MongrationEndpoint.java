package com.kuliginstepan.mongration.actuator;

import com.kuliginstepan.mongration.entity.ChangesetEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.data.mongodb.core.MongoTemplate;

@Endpoint(id = "mongration")
@RequiredArgsConstructor
public class MongrationEndpoint {

    private final MongoTemplate template;

    @ReadOperation
    public List<ChangesetEntity> getExecutedChangesets() {
        return template.findAll(ChangesetEntity.class);
    }
}
