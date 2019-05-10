package changelogs;

import com.kuliginstepan.mongration.TestEntityRepository;
import com.kuliginstepan.mongration.annotation.ChangeLog;
import com.kuliginstepan.mongration.annotation.ChangeSet;

@ChangeLog
public class UnexpectedChangeLog14 {

    @ChangeSet(order = 1, id = "change1", author = "Stepan")
    public void migration(TestEntityRepository repository) {
    }
}
