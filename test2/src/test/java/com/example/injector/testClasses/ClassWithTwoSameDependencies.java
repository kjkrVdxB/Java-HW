package com.example.injector.testClasses;

public class ClassWithTwoSameDependencies {
    public final ClassWithoutDependencies dependency1;
    public final ClassWithoutDependencies dependency2;

    public ClassWithTwoSameDependencies(ClassWithoutDependencies dependency1, ClassWithoutDependencies dependency2) {
        this.dependency1 = dependency1;
        this.dependency2 = dependency2;
    }
}
