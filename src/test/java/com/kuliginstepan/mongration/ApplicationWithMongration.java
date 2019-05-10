package com.kuliginstepan.mongration;

import com.kuliginstepan.mongration.testconfiguration.TestMongrationAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    TestMongrationAutoConfiguration.class,
    ApplicationWithTestMongration.class}))
public class ApplicationWithMongration {

}
