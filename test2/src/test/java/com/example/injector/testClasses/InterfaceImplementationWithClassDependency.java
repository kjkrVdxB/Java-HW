package com.example.injector.testClasses;

public class InterfaceImplementationWithClassDependency implements Interface {
    public final ClassWithOneInterfaceDependency dependency;

    public InterfaceImplementationWithClassDependency(ClassWithOneInterfaceDependency dependency) {
        this.dependency = dependency;
    }
}
