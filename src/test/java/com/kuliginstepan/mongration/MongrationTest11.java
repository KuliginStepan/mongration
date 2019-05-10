package com.kuliginstepan.mongration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationWithMongration.class)
@DirtiesContext
public class MongrationTest11 {

    @Test
    public void contextLoads() {
    }
}
