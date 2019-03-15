package com.example.injector.testClasses;

public class ClassWithoutDependencies {
    public static int constructorCallCount = 0;

    public ClassWithoutDependencies() {
        ++constructorCallCount;
    }
}