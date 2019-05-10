package changelogs;

import com.kuliginstepan.mongration.TestComponent;
import com.kuliginstepan.mongration.annotation.ChangeLog;
import com.kuliginstepan.mongration.annotation.ChangeSet;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeLog
@RequiredArgsConstructor
public class SimpleChangeLog17 {

    private final TestComponent component;

    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    public void migration(MongoTemplate template) {
        template.save(new Document("index", "1").append("text", "1"), "entity");
        template.save(new Document("index", "2").append("text", "2"), "entity");
        template.save(new Document("index", "3").append("text", "3"), "entity");
    }

    @ChangeSet(order = 2, id = "change2", author = "Stepan", runAlways = true)
    public void migration1(MongoTemplate template) {
        template.findAll(Document.class, "entity").forEach(document -> {
            document.computeIfPresent("text", (s, v) -> v + component.getPrefix());
            template.save(document, "entity");
        });
    }
}
