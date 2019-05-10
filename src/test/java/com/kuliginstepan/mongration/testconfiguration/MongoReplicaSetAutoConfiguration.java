package com.kuliginstepan.mongration.testconfiguration;

import com.kuliginstepan.mongration.configuration.MongrationAutoConfiguration;
import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(MongrationAutoConfiguration.class)
@RequiredArgsConstructor
@Slf4j
public class MongoReplicaSetAutoConfiguration {

    private final MongoClient client;

    @Value("${local.mongo.port}")
    private int port;

    @PostConstruct
    public void init() {
        MongoDatabase admin = client.getDatabase("admin");
        Document config = new Document("_id", "rs0");
        BasicDBList members = new BasicDBList();
        String host = "localhost:" + port;
        members.add(new Document("_id", 0)
            .append("host", host));
        config.put("members", members);
        admin.runCommand(new Document("replSetInitiate", config));
    }
}
