package initdata;

import com.mongodb.MongoClient;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@RequiredArgsConstructor
public class MongrationTest12Configuration {

    private final MongoClient client;

    @PostConstruct
    public void init() {
        client.getDatabase("skuligin_test").getCollection("mongration_changelogs").insertOne(new Document("_id", "LOCK"));
    }
}