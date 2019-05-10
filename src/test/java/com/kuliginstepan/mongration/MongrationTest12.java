package com.kuliginstepan.mongration;

import static org.junit.Assert.assertEquals;

import changelogs.SimpleChangeLog1;
import initdata.MongrationTest12Configuration;
import java.util.List;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationWithTestMongration.class)
@Import({SimpleChangeLog1.class, MongrationTest12Configuration.class})
public class MongrationTest12 {

    @Autowired
    TestEntityRepository repository;
    @Autowired
    MongoTemplate template;

    @Test
    public void test() {
        List<TestEntity> entities = repository.findAll();
        List<Document> migrations = template.findAll(Document.class, "mongration_changelogs");

        assertEquals(0, entities.size());
        assertEquals("LOCK", migrations.get(0).getString("_id"));
        assertEquals(1, migrations.size());
    }
}
