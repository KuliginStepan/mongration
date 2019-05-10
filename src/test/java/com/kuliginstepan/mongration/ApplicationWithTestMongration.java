package com.kuliginstepan.mongration;

import com.kuliginstepan.mongration.configuration.MongrationAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {MongrationAutoConfiguration.class,
    ApplicationWithMongration.class}))
public class ApplicationWithTestMongration {

}
