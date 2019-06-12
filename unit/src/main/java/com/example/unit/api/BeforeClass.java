package com.example.unit.api;

import java.lang.annotation.*;

/**
 * Methods in a test class annotated with this annotation will be run after all tests once.
 * Such methods must be static.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeClass {
}
