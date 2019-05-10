package initdata;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@RequiredArgsConstructor
public class MongrationTest7Configuration {

    private final MongoClient client;

    @PostConstruct
    public void init() {
        MongoCollection<Document> collection = client.getDatabase("skuligin_test").getCollection("entity");
        collection.insertOne(new Document("text", "1"));
        collection.insertOne(new Document("text", "2"));
        collection.insertOne(new Document("text", "3"));
    }
}