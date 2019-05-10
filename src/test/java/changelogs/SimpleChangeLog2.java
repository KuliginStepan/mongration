package changelogs;

import com.kuliginstepan.mongration.annotation.ChangeLog;
import com.kuliginstepan.mongration.annotation.ChangeSet;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeLog
public class SimpleChangeLog2 {

    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    public void migration(MongoTemplate template){
        template.findAll(Document.class, "entity").forEach(document -> {
            document.computeIfPresent("text", (s, v) -> v + "-updated");
            template.save(document, "entity");
        });
    }
}
