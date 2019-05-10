package changelogs;

import com.kuliginstepan.mongration.annotation.ChangeLog;
import com.kuliginstepan.mongration.annotation.ChangeSet;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@ChangeLog
public class SimpleChangeLog9 {

    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    public void migration(MongoTemplate template, TransactionTemplate txTemplate){
        template.createCollection("entity");
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                template.save(new Document("index", "1").append("text", "1"), "entity");
                template.save(new Document("index", "2").append("text", "2"), "entity");
                template.save(new Document("index", "3").append("text", "3"), "entity");
            }
        });
        }
}
