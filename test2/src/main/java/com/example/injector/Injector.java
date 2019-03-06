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
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     *
     * @throws ClassNotFoundException if one of the classes was not found
     * @throws IllegalAccessException if constructor could not be called because of access violation
     * @throws InstantiationException if constructor could not be called
     * @throws InvocationTargetException
     * @throws InjectionCycleException if dependency cycle was found
     * @throws AmbiguousImplementationException if there were multiple implementations for required class
     * @throws ImplementationNotFoundException if one of the dependencies could not be fulfilled
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
        Class<?> clazz = Class.forName(className);
        availableImplementations.add(clazz);
        for (var implementationName: implementations) {
            availableImplementations.add(Class.forName(implementationName));
        }
        for (var implementation: availableImplementations) {
            statuses.put(implementation, Status.IGNORED);
        }
        var result = inject(clazz);
        statuses.clear();
        instances.clear();;
        availableImplementations.clear();
        return result;
    }

    private static Object inject(Class<?> clazz) throws IllegalAccessException,
                                                        InvocationTargetException,
                                                        InstantiationException,
                                                        InjectionCycleException,
                                                        AmbiguousImplementationException,
                                                        ImplementationNotFoundException {
        var status = statuses.get(clazz);
        if (status == Status.LOADED) {
            return instances.get(clazz);
        }
        if (status == Status.IN_PROGRESS) {
            throw new InjectionCycleException();
        }
        statuses.put(clazz, Status.IN_PROGRESS);
        var accumulatedParameters = new ArrayList<Object>();
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
