package com.example.reflector;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Comparator;

/** Class for comparing two Java classes. */
public class JavaDiff {
    private static final String IN_FIRST_PREFIX = "< ";
    private static final String IN_SECOND_PREFIX = "> ";
    private final PrintWriter writer;
    private final JavaPrinter javaPrinter;

    /** Create new {@code JavaDiff} instance with supplied {@code PrintWriter}. */
    public JavaDiff(@NonNull PrintWriter writer) {
        Validate.notNull(writer, "writer can not be null");

        this.writer = writer;
        javaPrinter = new JavaPrinter(writer);
    }

    /** Print differences between classes' fields and methods. */
    public void diffClasses(@NonNull Class<?> classA, @NonNull Class<?> classB) {
        Validate.notNull(classA, "classA can not be null");
        Validate.notNull(classB, "classB can not be null");

        diffFields(classA, classB);
        diffMethods(classA, classB);
    }

    /** Print differences between classes' fields. */
    private void diffFields(@NonNull Class<?> classA, @NonNull Class<?> classB) {
        // sort fields so that the output is stable
        var fieldsA = classA.getDeclaredFields();
        Arrays.sort(fieldsA, Comparator.comparing(Field::getName));
        for (var fieldA: fieldsA) {
            Field fieldB;
            try {
                fieldB = classB.getDeclaredField(fieldA.getName());
            } catch (NoSuchFieldException e) {
                writer.print(IN_FIRST_PREFIX);
                javaPrinter.printField(fieldA);
                writer.println();
                continue;
            }
            if (compareFields(fieldA, fieldB) != 0) {
                writer.print(IN_FIRST_PREFIX);
                javaPrinter.printField(fieldA);
                writer.println();
                writer.print(IN_SECOND_PREFIX);
                javaPrinter.printField(fieldB);
                writer.println();
            }
        }
        // sort fields so that the output is stable
        var fieldsB = classB.getDeclaredFields();
        Arrays.sort(fieldsB, Comparator.comparing(Field::getName));
        for (var fieldB: fieldsB) {
            try {
                classA.getDeclaredField(fieldB.getName());
            } catch (NoSuchFieldException e) {
                // the only fields in B we still did not inspect are the ones that do not have a one with the same name
                // in A. So we just print all these.
                writer.print(IN_SECOND_PREFIX);
                javaPrinter.printField(fieldB);
                writer.println();
            }
        }
    }

    /** Print differences between classes' methods. */
    private void diffMethods(@NonNull Class<?> classA, @NonNull Class<?> classB) {
        // Despite the code is very similar to diffFields, the amount of differing elements used
        // (getDeclaredMethod(s), NoSuchMethodException, compareMethods, printMethodsOneLine,
        // compereMethodsSignatureOnly) makes it unreasonable to unify them.

        // sort methods so that the output is stable
        var methodsA = classA.getDeclaredMethods();
        Arrays.sort(methodsA, JavaDiff::compareMethodsSignatureOnly);
        for (var methodA: methodsA) {
            Method methodB;
            try {
                methodB = classB.getDeclaredMethod(methodA.getName(), methodA.getParameterTypes());
            } catch (NoSuchMethodException e) {
                writer.print(IN_FIRST_PREFIX);
                javaPrinter.printMethodOneLine(methodA);
                writer.println();
                continue;
            }
            if (compareMethods(methodA, methodB) != 0) {
                writer.print(IN_FIRST_PREFIX);
                javaPrinter.printMethodOneLine(methodA);
                writer.println();
                writer.print(IN_SECOND_PREFIX);
                javaPrinter.printMethodOneLine(methodB);
                writer.println();
            }
        }
        // sort methods so that the output is stable
        var methodsB = classB.getDeclaredMethods();
        Arrays.sort(methodsB, JavaDiff::compareMethodsSignatureOnly);
        for (var methodB: methodsB) {
            try {
                classA.getDeclaredMethod(methodB.getName(), methodB.getParameterTypes());
            } catch (NoSuchMethodException e) {
                // the only methods in B we still did not inspect are the ones that do not have a one with the same name
                // and signature in A. So we just print all these.
                writer.print(IN_SECOND_PREFIX);
                javaPrinter.printMethodOneLine(methodB);
                writer.println();
            }
        }
    }

    /** Compare based on erased types only, useful for sorting */
    static int compareMethodsSignatureOnly(@NonNull Method methodA, @NonNull Method methodB) {
        int namesDiff = methodA.getName().compareTo(methodB.getName());
        if (namesDiff != 0) {
            return namesDiff;
        }
        return Arrays.compare(methodA.getParameterTypes(), methodB.getParameterTypes(), JavaDiff::compareClasses);
    }

    /** Compare based on erased types only, useful for sorting */
    static int compareConstructorsSignatureOnly(@NonNull Constructor constructorA, @NonNull Constructor constructorB) {
        return Arrays.compare(constructorA.getParameterTypes(),
                              constructorB.getParameterTypes(),
                              JavaDiff::compareClasses);
    }

    private static int compareFields(@NonNull Field fieldA, @NonNull Field fieldB) {
        int namesDiff = fieldA.getName().compareTo(fieldB.getName());
        if (namesDiff != 0) {
            return namesDiff;
        }

        int modifiersDiff = Integer.compare(fieldA.getModifiers(), fieldB.getModifiers());
        if (modifiersDiff != 0) {
            return modifiersDiff;
        }

        int annotationListsDiff = compareAnnotationLists(fieldA.getDeclaredAnnotations(),
                                                         fieldB.getDeclaredAnnotations());
        if (annotationListsDiff != 0) {
            return annotationListsDiff;
        }

        return compareAnnotatedTypes(fieldA.getAnnotatedType(), fieldB.getAnnotatedType());
    }

    private static int compareMethods(@NonNull Method methodA, @NonNull Method methodB) {
        int namesDiff = methodA.getName().compareTo(methodB.getName());
        if (namesDiff != 0) {
            return namesDiff;
        }

        int modifiersDiff = Integer.compare(methodA.getModifiers(), methodB.getModifiers());
        if (modifiersDiff != 0) {
            return modifiersDiff;
        }

        int annotationListsDiff = compareAnnotationLists(methodA.getDeclaredAnnotations(),
                                                         methodB.getDeclaredAnnotations());
        if (annotationListsDiff != 0) {
            return annotationListsDiff;
        }

        int returnTypesDiff = compareAnnotatedTypes(methodA.getAnnotatedReturnType(), methodB.getAnnotatedReturnType());
        if (returnTypesDiff != 0) {
            return returnTypesDiff;
        }

        int exceptionListsDiff = compareSorted(methodA.getAnnotatedExceptionTypes(),
                                               methodB.getAnnotatedExceptionTypes(),
                                               JavaDiff::compareAnnotatedTypes);
        if (exceptionListsDiff != 0) {
            return exceptionListsDiff;
        }

        int typeParametersDiff = compareSorted(methodA.getTypeParameters(),
                                               methodB.getTypeParameters(),
                                               JavaDiff::compareTypeVariables);
        if (typeParametersDiff != 0) {
            return typeParametersDiff;
        }

        return Arrays.compare(methodA.getParameters(), methodB.getParameters(), JavaDiff::compareParameters);
    }

    private static int compareAnnotatedTypes(@NonNull AnnotatedType typeA,
                                             @NonNull AnnotatedType typeB) {
        int annotationListsDiff = compareAnnotationLists(typeA.getDeclaredAnnotations(),
                                                         typeB.getDeclaredAnnotations());
        if (annotationListsDiff != 0) {
            return annotationListsDiff;
        }

        if (typeA instanceof AnnotatedParameterizedType || typeB instanceof AnnotatedParameterizedType) {
            return compareIsInstanceAndThen(typeA, typeB, AnnotatedParameterizedType.class,
                                            JavaDiff::compareAnnotatedParametrizedTypes);
        }

        if (typeA instanceof AnnotatedTypeVariable || typeB instanceof AnnotatedTypeVariable) {
            return compareIsInstanceAndThen(typeA, typeB, AnnotatedTypeVariable.class,
                                            JavaDiff::compareAnnotatedTypeVariables);
        }

        if (typeA instanceof AnnotatedArrayType || typeB instanceof AnnotatedArrayType) {
            return compareIsInstanceAndThen(typeA, typeB, AnnotatedArrayType.class,
                                            JavaDiff::compareAnnotatedArrayTypes);
        }

        if (typeA instanceof AnnotatedWildcardType || typeB instanceof AnnotatedWildcardType) {
            return compareIsInstanceAndThen(typeA, typeB, AnnotatedWildcardType.class,
                                            JavaDiff::compareAnnotatedWildcardTypes);
        }

        assert typeA.getType() instanceof Class;
        assert typeB.getType() instanceof Class;
        return compareClasses((Class<?>) typeA.getType(), (Class<?>) typeB.getType());
    }

    /**
     * If one of the objects is instance of given class, it is considered greater.
     * If both are, the comparator is used.
     */
    private static <T, U> int compareIsInstanceAndThen(T a, T b, Class<U> clazz, Comparator<? super U> comparator) {
        if (clazz.isInstance(a)) {
            if (!clazz.isInstance(b)) {
                return 1;
            }
            return comparator.compare(clazz.cast(a), clazz.cast(b));
        }
        return clazz.isInstance(b) ? -1 : 0;
    }

    private static int compareAnnotatedWildcardTypes(@NonNull AnnotatedWildcardType typeA,
                                                     @NonNull AnnotatedWildcardType typeB) {
        int annotationsDiff = compareAnnotationLists(typeA.getDeclaredAnnotations(), typeB.getDeclaredAnnotations());
        if (annotationsDiff != 0) {
            return annotationsDiff;
        }

        var upperBoundsA = typeA.getAnnotatedUpperBounds();
        var upperBoundsB = typeB.getAnnotatedUpperBounds();
        int upperBoundsDiff = compareSorted(upperBoundsA, upperBoundsB, JavaDiff::compareAnnotatedTypes);
        if (upperBoundsDiff != 0) {
            return upperBoundsDiff;
        }

        var lowerBoundsA = typeA.getAnnotatedLowerBounds();
        var lowerBoundsB = typeB.getAnnotatedLowerBounds();
        return compareSorted(lowerBoundsA, lowerBoundsB, JavaDiff::compareAnnotatedTypes);
    }

    private static int compareAnnotatedTypeVariables(@NonNull AnnotatedTypeVariable typeA,
                                                     @NonNull AnnotatedTypeVariable typeB) {
        int namesDiff = typeA.getType().getTypeName().compareTo(typeB.getType().getTypeName());
        if (namesDiff != 0) {
            return namesDiff;
        }

        int annotationsDiff = compareAnnotationLists(typeA.getDeclaredAnnotations(), typeB.getDeclaredAnnotations());
        if (annotationsDiff != 0) {
            return annotationsDiff;
        }

        var upperBoundsA = typeA.getAnnotatedBounds();
        var upperBoundsB = typeB.getAnnotatedBounds();
        return compareSorted(upperBoundsA, upperBoundsB, JavaDiff::compareAnnotatedTypes);
    }

    // TypeVariable and AnnotatedTypeVariable are quite different interfaces, both of them have annotations though
    private static int compareTypeVariables(@NonNull TypeVariable typeA,
                                            @NonNull TypeVariable typeB) {
        int namesDiff = typeA.getName().compareTo(typeB.getName());
        if (namesDiff != 0) {
            return namesDiff;
        }

        int annotationsDiff = compareAnnotationLists(typeA.getDeclaredAnnotations(), typeB.getDeclaredAnnotations());
        if (annotationsDiff != 0) {
            return annotationsDiff;
        }

        var upperBoundsA = typeA.getAnnotatedBounds();
        var upperBoundsB = typeB.getAnnotatedBounds();
        return compareSorted(upperBoundsA, upperBoundsB, JavaDiff::compareAnnotatedTypes);
    }

    private static int compareAnnotatedArrayTypes(@NonNull AnnotatedArrayType typeA,
                                                  @NonNull AnnotatedArrayType typeB) {
        return compareAnnotatedTypes(typeA.getAnnotatedGenericComponentType(),
                                     typeB.getAnnotatedGenericComponentType());
    }

    private static int compareAnnotatedParametrizedTypes(@NonNull AnnotatedParameterizedType typeA,
                                                         @NonNull AnnotatedParameterizedType typeB) {
        assert typeA.getType() instanceof ParameterizedType;
        assert typeB.getType() instanceof ParameterizedType;
        var rawTypeA = ((ParameterizedType) typeA.getType()).getRawType();
        var rawTypeB = ((ParameterizedType) typeB.getType()).getRawType();
        assert rawTypeA instanceof Class;
        assert rawTypeB instanceof Class;
        int rawTypesDiff = compareClasses((Class<?>) rawTypeA, (Class<?>) rawTypeB);
        if (rawTypesDiff != 0) {
            return rawTypesDiff;
        }

        return Arrays.compare(typeA.getAnnotatedActualTypeArguments(),
                              typeB.getAnnotatedActualTypeArguments(),
                              JavaDiff::compareAnnotatedTypes);
    }

    private static int compareClasses(@NonNull Class<?> classA, @NonNull Class<?> classB) {
        assert classA.getCanonicalName() != null;
        assert classB.getCanonicalName() != null;

        return classA.getCanonicalName().compareTo(classB.getCanonicalName());
    }

    private static int compareAnnotationLists(@NonNull Annotation[] annotationsA, @NonNull Annotation[] annotationsB) {
        return compareSorted(annotationsA, annotationsB, Comparator.comparing(Annotation::toString));
    }

    private static int compareParameters(@NonNull Parameter a, @NonNull Parameter b) {
        int annotationListsDiff = compareAnnotationLists(a.getDeclaredAnnotations(), a.getDeclaredAnnotations());
        if (annotationListsDiff != 0) {
            return annotationListsDiff;
        }

        return compareAnnotatedTypes(a.getAnnotatedType(), b.getAnnotatedType());
    }

    /** Sort arrays an then compare them. NB: modifies arrays. */
    private static <T> int compareSorted(@NonNull T[] a, @NonNull T[] b, @NonNull Comparator<? super T> comparator) {
        Arrays.sort(a, comparator);
        Arrays.sort(b, comparator);
        return Arrays.compare(a, b, comparator);
    }
}
