package initdata;

import changelogs.SimpleChangeLog16;
import com.mongodb.MongoClient;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@RequiredArgsConstructor
public class MongrationTest16Configuration {

    private final MongoClient client;

    @PostConstruct
    public void init() {
        client.getDatabase("skuligin_test").getCollection("mongration_changelogs")
            .insertOne(new Document("changeSetId", "change2").append("changeLogClass", SimpleChangeLog16.class.getName()));
    }
}