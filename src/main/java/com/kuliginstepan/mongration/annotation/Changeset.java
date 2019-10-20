package com.kuliginstepan.mongration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * Annotation for marking method as changeset
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Changeset {

    /**
     * Author of the changeset
     *
     * @return author
     */
    String author();

    /**
     * Default value is {@link Method#getName()}
     *
     * @return changeset id
     */
    String id() default "";

    /**
     * Sequence that provide correct order for changesets. Sorted ascending. Must be unique within changelog
     *
     * @return changeset order
     */
    int order();

}
