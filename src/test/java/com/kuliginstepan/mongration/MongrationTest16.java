package com.kuliginstepan.mongration;

import static org.junit.Assert.assertEquals;

import changelogs.SimpleChangeLog16;
import initdata.MongrationTest16Configuration;
import java.util.List;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationWithTestMongration.class)
@Import({SimpleChangeLog16.class, MongrationTest16Configuration.class})
@ActiveProfiles("db")
public class MongrationTest16 {

    @Autowired
    TestEntityRepository repository;
    @Autowired
    MongoTemplate template;

    @Test
    public void test() {
        List<TestEntity> entities = repository.findAll();
        List<Document> migrations = template.findAll(Document.class, "mongration_changelogs");

        assertEquals("1", entities.get(0).getIndex());
        assertEquals("1-updated", entities.get(0).getText());
        assertEquals("2", entities.get(1).getIndex());
        assertEquals("2-updated", entities.get(1).getText());
        assertEquals("3", entities.get(2).getIndex());
        assertEquals("3-updated", entities.get(2).getText());

        assertEquals("change2", migrations.get(0).getString("changeSetId"));
        assertEquals(SimpleChangeLog16.class.getName(), migrations.get(0).getString("changeLogClass"));

        assertEquals("change1", migrations.get(1).getString("changeSetId"));
        assertEquals("Stepan", migrations.get(1).getString("author"));
        assertEquals(SimpleChangeLog16.class.getName(), migrations.get(1).getString("changeLogClass"));
        assertEquals("migration", migrations.get(1).getString("changeSetMethod"));

        assertEquals(3, entities.size());
        assertEquals(2, migrations.size());
    }
}
