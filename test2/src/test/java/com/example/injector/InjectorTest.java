package com.example.injector;

import com.example.injector.testClasses.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class InjectorTest {
    @Test
    public void injectorShouldInitializeClassWithoutDependencies()
            throws Exception {
        var object = Injector.initialize("com.example.injector.testClasses.ClassWithoutDependencies", Collections.emptyList());
        assertTrue(object instanceof ClassWithoutDependencies);
    }

    @Test
    public void injectorShouldInitializeClassWithOneClassDependency()
            throws Exception {
        var object = Injector.initialize(
                "com.example.injector.testClasses.ClassWithOneClassDependency",
                Collections.singletonList("com.example.injector.testClasses.ClassWithoutDependencies")
        );
        assertTrue(object instanceof ClassWithOneClassDependency);
        var instance = (ClassWithOneClassDependency) object;
        assertNotNull(instance.dependency);
    }

    @Test
    public void injectorShouldInitializeClassWithOneInterfaceDependency()
            throws Exception {
        var object = Injector.initialize(
                "com.example.injector.testClasses.ClassWithOneInterfaceDependency",
                Collections.singletonList("com.example.injector.testClasses.InterfaceImpl")
        );
        assertTrue(object instanceof ClassWithOneInterfaceDependency);
        ClassWithOneInterfaceDependency instance = (ClassWithOneInterfaceDependency) object;
        assertTrue(instance.dependency instanceof InterfaceImpl);
    }

    @Test
    public void injectorShouldDetectDependencyCycle() {
        assertThrows(InjectionCycleException.class, () -> Injector.initialize(
                "com.example.injector.testClasses.ClassWithOneInterfaceDependency",
                List.of("com.example.injector.testClasses.InterfaceImplementationWithClassDependency")
        ));
    }

    @Test
    public void injectorShouldNotLoadMoreThanOnce() throws Exception {
        int startCount = ClassWithoutDependencies.constructorCallCount;
        var object = Injector.initialize(
                "com.example.injector.testClasses.ClassWithTwoSameDependencies",
                Collections.singletonList("com.example.injector.testClasses.ClassWithoutDependencies")
        );
        assertTrue(object instanceof ClassWithTwoSameDependencies);
        var instance = (ClassWithTwoSameDependencies) object;
        assertNotNull(instance.dependency1);
        assertNotNull(instance.dependency2);
        assertEquals(1, ClassWithoutDependencies.constructorCallCount - startCount);
    }

    @Test
    public void injectorShouldDetectAmbiguousImplementations() throws Exception {
        assertThrows(AmbiguousImplementationException.class, () -> Injector.initialize(
                "com.example.injector.testClasses.ClassWithOneInterfaceDependency",
                List.of("com.example.injector.testClasses.InterfaceImpl",
                        "com.example.injector.testClasses.AnotherInterfaceImpl")
        ));
    }

    @Test
    public void injectorShouldDetectAnavailableDependency() throws Exception {
        assertThrows(ImplementationNotFoundException.class, () -> Injector.initialize(
                "com.example.injector.testClasses.ClassWithOneInterfaceDependency",
                List.of("com.example.injector.testClasses.ClassWithoutDependencies")
        ));
    }

    @Test
    public void testNullParameters() {
        assertThrows(IllegalArgumentException.class, () -> Injector.initialize(null, Collections.emptyList()));
        assertThrows(IllegalArgumentException.class,
                     () -> Injector.initialize("com.example.injector.testClasses.ClassWithoutDependencies",
                                               Collections.singletonList(null)));
    }
}