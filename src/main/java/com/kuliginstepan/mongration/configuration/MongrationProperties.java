package com.kuliginstepan.mongration.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mongration")
public class MongrationProperties {

    /**
     * Changelogs collection name
     */
    private String changelogsCollection = "mongration_changelogs";
}
