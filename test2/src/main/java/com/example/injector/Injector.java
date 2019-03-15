package com.example.injector;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Injector {
    private enum Status {
        IGNORED,
        IN_PROGRESS,
        LOADED
    }

    private static HashMap<Class<?>, Status> statuses = new HashMap<>();
    private static HashMap<Class<?>, Object> instances = new HashMap<>();
    private static ArrayList<Class<?>> availableImplementations = new ArrayList<>();


    /**
     * Create and initialize object of {@code className} class using classes from
     * {@code implementations} for concrete dependencies.
     *
     * @throws ClassNotFoundException           if one of the classes was not found
     * @throws IllegalAccessException           if constructor could not be called because of access violation
     * @throws InstantiationException           if constructor could not be called
     * @throws InvocationTargetException        if constructor could not be called
     * @throws InjectionCycleException          if dependency cycle was found
     * @throws AmbiguousImplementationException if there were multiple implementations for required class
     * @throws ImplementationNotFoundException  if one of the dependencies could not be fulfilled
     */
    public static Object initialize(String className, List<String> implementations) throws ClassNotFoundException,
                                                                                           IllegalAccessException,
                                                                                           InstantiationException,
                                                                                           InvocationTargetException,
                                                                                           InjectionCycleException,
                                                                                           AmbiguousImplementationException,
                                                                                           ImplementationNotFoundException {
        if (className == null) {
            throw new IllegalArgumentException("className can not be null");
        }
        for (var implementationName : implementations) {
            if (implementationName == null) {
                throw new IllegalArgumentException("implementation class name can not be null");
            }
        }
        Class<?> clazz = Class.forName(className);
        availableImplementations.add(clazz);
        for (var implementationName : implementations) {
            availableImplementations.add(Class.forName(implementationName));
        }
        for (var implementation : availableImplementations) {
            statuses.put(implementation, Status.IGNORED);
        }
        Object result;
        try {
            result = inject(clazz);
        } finally {
            statuses.clear();
            instances.clear();
            availableImplementations.clear();
        }
        return result;
    }

    private static Object inject(Class<?> clazz) throws IllegalAccessException,
                                                        InvocationTargetException,
                                                        InstantiationException,
                                                        InjectionCycleException,
                                                        AmbiguousImplementationException,
                                                        ImplementationNotFoundException {
        assert clazz != null;
        var status = statuses.get(clazz);
        if (status == Status.LOADED) {
            return instances.get(clazz);
        }
        if (status == Status.IN_PROGRESS) {
            throw new InjectionCycleException();
        }
        statuses.put(clazz, Status.IN_PROGRESS);
        var accumulatedParameters = new ArrayList<>();
        for (var dependency : getDependencies(clazz)) {
            accumulatedParameters.add(inject(findImplementation(dependency)));
        }
        Object newInstance = clazz.getDeclaredConstructors()[0].newInstance(accumulatedParameters.toArray());
        instances.put(clazz, newInstance);
        statuses.put(clazz, Status.LOADED);
        return newInstance;
    }

    private static Class<?> findImplementation(Class<?> clazz) throws AmbiguousImplementationException,
                                                                      ImplementationNotFoundException {
        assert clazz != null;
        Class<?> chosenImplementation = null;
        for (var implementation : availableImplementations) {
            if (clazz.isAssignableFrom(implementation)) {
                if (chosenImplementation != null) {
                    throw new AmbiguousImplementationException();
                }
                chosenImplementation = implementation;
            }
        }
        if (chosenImplementation == null) {
            throw new ImplementationNotFoundException();
        }
        return chosenImplementation;
    }

    private static Class<?>[] getDependencies(Class<?> clazz) {
        assert clazz != null;
        assert clazz.getDeclaredConstructors().length == 1;
        var constructor = clazz.getDeclaredConstructors()[0];
        return constructor.getParameterTypes();
    }
}
