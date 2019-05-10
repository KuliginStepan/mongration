package initdata;

import com.mongodb.MongoClient;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@RequiredArgsConstructor
public class MongrationTest8Configuration {

    private final MongoClient client;

    @PostConstruct
    public void init() {
        client.getDatabase("skuligin_test").createCollection("entity");
    }
}