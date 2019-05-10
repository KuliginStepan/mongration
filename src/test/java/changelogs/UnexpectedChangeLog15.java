package changelogs;

import com.kuliginstepan.mongration.TestEntityRepository;
import com.kuliginstepan.mongration.annotation.ChangeLog;
import com.kuliginstepan.mongration.annotation.ChangeSet;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeLog
public class UnexpectedChangeLog15 {

    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    public void migration(MongoTemplate template, TestEntityRepository repository) {
    }
}
