package com.kuliginstepan.mongration.configuration;

import com.kuliginstepan.mongration.AbstractMongration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Configuration properties used to configure {@link AbstractMongration}
 */
@Data
@ConfigurationProperties(prefix = "mongration")
public class MongrationProperties {

    public static final String ENABLED_PROPERTY = "mongration.enabled";

    /**
     * Changelogs collection name
     */
    private String changelogsCollection = "mongration_changelogs";

    /**
     * Enable or disable mongration
     */
    private boolean enabled = true;

    /**
     * Mode for running changesets
     */
    private MongrationMode mode = MongrationMode.AUTO;
}
