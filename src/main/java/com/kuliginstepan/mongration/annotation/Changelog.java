package com.kuliginstepan.mongration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * Annotation for marking a class as a changelog
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Changelog {

    /**
     * Alias for {@link #id()}
     * @return Changelog id
     */
    @AliasFor("id")
    String value() default "";

    /**
     * Default value is {@link Class#getSimpleName()}
     * @return Changelog id
     */
    @AliasFor("value")
    String id() default "";
}
