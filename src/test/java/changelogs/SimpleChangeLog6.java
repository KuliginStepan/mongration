package changelogs;

import com.kuliginstepan.mongration.annotation.ChangeLog;
import com.kuliginstepan.mongration.annotation.ChangeSet;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeLog
public class SimpleChangeLog6 {

    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    public void migration(MongoTemplate template){
        template.save(new Document("index", "1").append("text", "1"), "entity");
        template.save(new Document("index", "2").append("text", "2"), "entity");
        template.save(new Document("index", "3").append("text", "3"), "entity");
    }

    @ChangeSet(order = 2, id = "change2", author = "Stepan")
    public void migration1(MongoTemplate template){
        throw new RuntimeException("Exception while executing migration");
    }
}
