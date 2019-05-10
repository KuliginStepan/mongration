package changelogs;

import com.kuliginstepan.mongration.TestEntityRepository;
import com.kuliginstepan.mongration.annotation.ChangeLog;
import com.kuliginstepan.mongration.annotation.ChangeSet;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@ChangeLog
public class UnexpectedChangeLog13 {

    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    public void migration(TransactionTemplate txTemplate, MongoTemplate template, TestEntityRepository repository) {
    }
}
