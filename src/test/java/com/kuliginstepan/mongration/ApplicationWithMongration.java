package com.kuliginstepan.mongration;

import com.kuliginstepan.mongration.configuration.MongrationAutoConfiguration;
import com.kuliginstepan.mongration.testconfiguration.TestMongrationAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    TestMongrationAutoConfiguration.class, MongrationAutoConfiguration.class,
    ApplicationWithTestMongration.class}))
public class ApplicationWithMongration {

}
