package changelogs;

import com.kuliginstepan.mongration.annotation.ChangeLog;
import com.kuliginstepan.mongration.annotation.ChangeSet;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.Transactional;

@ChangeLog
public class SimpleChangeLog8 {

    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    @Transactional
    public void migration(MongoTemplate template) {
        template.save(new Document("index", "1").append("text", "1"), "entity");
        template.save(new Document("index", "2").append("text", "2"), "entity");
        template.save(new Document("index", "3").append("text", "3"), "entity");
    }
}
