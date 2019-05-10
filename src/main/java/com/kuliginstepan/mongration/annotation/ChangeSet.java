package com.kuliginstepan.mongration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for changelog-class method`s marking as changeset
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeSet {

    /**
     * Author of the changeset.
     * @return author
     */
    String author();

    /**
     * Unique ID of the changeset. Must be unique on changelog`s level
     * @return unique id
     */
    String id();

    /**
     * Sequence that provide correct order for changesets. Sorted ascending.
     * @return ordering
     */
    int order();

    /**
     * Executes the change set on every mongration`s execution, even if it has been run before.
     * @return should run always?
     */
    boolean runAlways() default false;
}
