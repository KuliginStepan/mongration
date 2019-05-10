package com.kuliginstepan.mongration;

import static org.junit.Assert.assertEquals;

import changelogs.SimpleChangeLog8;
import initdata.MongrationTest8Configuration;
import java.util.List;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationWithMongration.class, properties = "test.mongration.enable-transactions=true")
@Import({SimpleChangeLog8.class, MongrationTest8Configuration.class})
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class MongrationTest8 {

    @Autowired
    TestEntityRepository repository;
    @Autowired
    MongoTemplate template;

    @Test
    public void test() {
        List<TestEntity> entities = repository.findAll();
        List<Document> migrations = template.findAll(Document.class, "mongration_changelogs");

        assertEquals("1", entities.get(0).getIndex());
        assertEquals("1", entities.get(0).getText());
        assertEquals("2", entities.get(1).getIndex());
        assertEquals("2", entities.get(1).getText());
        assertEquals("3", entities.get(2).getIndex());
        assertEquals("3", entities.get(2).getText());

        assertEquals("change1", migrations.get(0).getString("changeSetId"));
        assertEquals("Stepan", migrations.get(0).getString("author"));
        assertEquals(SimpleChangeLog8.class.getName(), migrations.get(0).getString("changeLogClass"));
        assertEquals("migration", migrations.get(0).getString("changeSetMethod"));

        assertEquals(3, entities.size());
        assertEquals(1, migrations.size());
    }
}
