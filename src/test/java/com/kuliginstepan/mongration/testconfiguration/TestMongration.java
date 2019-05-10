package com.kuliginstepan.mongration.testconfiguration;

import com.kuliginstepan.mongration.Mongration;
import com.kuliginstepan.mongration.configuration.MongrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
public class TestMongration extends Mongration {

    public TestMongration(MongoTemplate template,
        TransactionTemplate txTemplate,
        MongrationProperties properties) {
        super(template, txTemplate, properties);
    }

    @Override
    public void afterPropertiesSet() {
        try {
            super.afterPropertiesSet();
        } catch (Exception e) {
            log.warn("Failed execute migrations", e);
        }
    }
}
