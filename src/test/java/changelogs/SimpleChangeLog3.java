package changelogs;

import com.kuliginstepan.mongration.annotation.ChangeLog;
import com.kuliginstepan.mongration.annotation.ChangeSet;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeLog
public class SimpleChangeLog3 {

    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    public void migration(MongoTemplate template) {
        throw new RuntimeException("Exception while executing migration");
    }
}
