package com.kuliginstepan.mongration.configuration;

import com.kuliginstepan.mongration.AbstractMongration;
import java.time.Duration;
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

    /**
     * Retry count for acquiring mongration lock
     */
    private int retryCount = 0;

    /**
     * Delay between retries on acquiring mongration lock
     */
    private Duration retryDelay = Duration.ofSeconds(4);
}
