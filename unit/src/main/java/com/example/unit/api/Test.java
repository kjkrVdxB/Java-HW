package com.example.unit.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods in a test class annotated with this annotation will be run as tests.
 * Such methods must not be static.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Test {
    /** Whether the test should be ignored. The value is the reason of it being ignored. */
    String ignore() default "";

    /**
     * Set to the exception expected to be thrown during the test invocation. {@code NoExceptionExpected.class} if no
     * exception is expected.
     */
    Class<? extends Exception> expected() default NoExceptionExpected.class;
}
