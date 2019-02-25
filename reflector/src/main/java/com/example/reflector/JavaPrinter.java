package com.example.reflector;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang3.math.NumberUtils.*;

import static org.apache.commons.lang3.math.NumberUtils.min;

/** Class for printing java structures to PrintWriter sequentially */
public class JavaPrinter {
    private int indentationLevel;
    private final PrintWriter writer;
    private boolean needIndentBetweenBraces;

    public JavaPrinter(@NonNull PrintWriter writer) {
        Validate.notNull(writer, "writer can not be null");

        indentationLevel = 0;
        this.writer = writer;
    }

    private void indent() {
        indent(false);
    }

    private void indent(boolean betweenBraces) {
        // all this is so an interface with no members is printed neatly
        if (betweenBraces && needIndentBetweenBraces) {
            writer.print(" ");
        } else {
            for (int i = 0; i < indentationLevel; ++i) {
                writer.print("    ");
            }
        }
        needIndentBetweenBraces = false;
    }

    private void printModifiers(int modifiers) {
        var asString = Modifier.toString(modifiers);
        writer.print(asString);
        if (!asString.isEmpty()) {
            writer.print(" ");
        }
    }

    // Classes and subclasses

    public void printClass(@NonNull Class<?> someClass) {
        Validate.notNull(someClass, "someClass can not be null");

        printClass(someClass, true);
    }

    private void printClass(@NonNull Class<?> someClass, boolean isTop) {
        if (isTop) {
            if (someClass.isMemberClass()) {
                throw new IllegalArgumentException("class should be on top level");
            }

            writer.println("package " + someClass.getPackageName() + ";");
            writer.println();
        }

        printAnnotationsMultiLine(someClass.getDeclaredAnnotations());
        indent();

        var type = "class";
        int modifiers = someClass.getModifiers();
        if (someClass.isInterface()) {
            modifiers &= ~Modifier.INTERFACE;
            modifiers &= ~Modifier.STATIC;
            modifiers &= ~Modifier.ABSTRACT;
            type = "interface";
        }
        if (someClass.isEnum()) {
            modifiers &= ~Modifier.STATIC;
            modifiers &= ~Modifier.FINAL;
            type = "enum";
        }
        if (isTop) {
            modifiers |= Modifier.PUBLIC; // force it to be public
            modifiers &= ~Modifier.STATIC; // should not be there on the top level
        }
        printModifiers(modifiers);

        writer.print(type + " " + someClass.getSimpleName());
        printTypeParameters(someClass.getTypeParameters());
        printAnnotatedSuperclass(someClass);
        printAnnotatedInterfaces(someClass);
        writer.print(" {");

        ++indentationLevel;

        needIndentBetweenBraces = true;

        if (someClass.isEnum()) {
            printEnumConstants(someClass);
        }
        printFields(someClass);
        printConstructors(someClass);
        printMethods(someClass);
        printSubclasses(someClass);

        --indentationLevel;

        indent(true);
        writer.println("}");
    }

    private void printAnnotatedInterfaces(@NonNull Class<?> someClass) {
        if (someClass.getAnnotatedInterfaces().length == 0) {
            return;
        }
        writer.print(someClass.isInterface() ? " extends " : " implements ");
        boolean first = true;
        for (var superinterface: someClass.getAnnotatedInterfaces()) {
            if (!first) {
                writer.println(", ");
            }
            printAnnotatedType(superinterface);
            first = false;
        }
    }

    private void printAnnotatedSuperclass(@NonNull Class<?> someClass) {
        if (!someClass.isEnum() && someClass.getAnnotatedSuperclass() != null &&
            (someClass.getSuperclass() != Object.class
             || someClass.getAnnotatedSuperclass().getAnnotations().length != 0)) {
            writer.print(" extends ");
            printAnnotatedType(someClass.getAnnotatedSuperclass());
        }
    }

    private void printSubclasses(@NonNull Class<?> someClass) {
        // sort subclasses so the output is stable
        var subclasses = someClass.getDeclaredClasses();
        Arrays.sort(subclasses, Comparator.comparing(Class::getSimpleName));
        for (var clazz: subclasses) {
            writer.println();
            printClass(clazz, false);
        }
    }

    // Generics

    /**
     * Despite this looks like printTypeArguments(), it is not actually the same since there is no way to get
     * AnnotatedTypeVariables of type parameters.
     */
    private void printTypeParameters(@NonNull TypeVariable<?>[] typeArguments) {
        if (typeArguments.length == 0) {
            return;
        }
        writer.print("<");
        boolean first = true;
        for (var typeVariable: typeArguments) {
            if (!first) {
                writer.print(", ");
            }
            printAnnotationsOneLine(typeVariable.getDeclaredAnnotations());
            writer.print(typeVariable.getName());
            boolean firstBound = true;
            for (var bound: typeVariable.getAnnotatedBounds()) {
                if (bound.getType() == Object.class) {
                    continue;
                }
                writer.print(firstBound ? " extends " : " & ");
                printAnnotatedType(bound);
                firstBound = false;
            }
            first = false;
        }
        writer.print(">");
    }

    // Methods and constructors

    private void printMethods(@NonNull Class<?> someClass) {
        // sort methods so the output is stable
        var methods = someClass.getDeclaredMethods();
        Arrays.sort(methods, JavaDiff::compareMethodsSignatureOnly);
        for (var method: methods) {
            if (method.isSynthetic()) {
                continue;
            }
            if (method.getDeclaringClass().isEnum()
                && method.getName().equals("values")
                && Arrays.equals(method.getParameterTypes(), new Class[]{})) {
                continue;
            }
            if (method.getDeclaringClass().isEnum()
                && method.getName().equals("valueOf")
                && Arrays.equals(method.getParameterTypes(), new Class[]{String.class})) {
                continue;
            }
            writer.println();
            printMethod(method);
        }
    }

    private void printMethod(@NonNull Method method) {
        printAnnotationsMultiLine(method.getDeclaredAnnotations());
        indent();
        printMethodHeader(method);
        if (Modifier.isAbstract(method.getModifiers()) && !method.isDefault()) {
            writer.println(";");
        } else {
            if (method.getReturnType() == void.class) {
                writer.println(" { }");
            } else {
                printBody(method.getReturnType());
            }
        }
    }

    private void printMethodHeader(@NonNull Method method) {
        int modifiers = method.getModifiers();
        if (method.getDeclaringClass().isInterface()) {
            modifiers &= ~Modifier.PUBLIC;
            modifiers &= ~Modifier.ABSTRACT;
        }
        if (method.isVarArgs()) {
            modifiers &= ~Modifier.TRANSIENT; // what were they even thinking?
        }
        printModifiers(modifiers);
        if (method.isDefault()) {
            writer.print("default ");
        }
        printTypeParameters(method.getTypeParameters());
        if (method.getTypeParameters().length != 0) {
            writer.print(" ");
        }
        printAnnotatedType(method.getAnnotatedReturnType());
        writer.print(" " + method.getName());
        printArgumentList(method.getParameters());
        printExceptions(method);
    }

    void printMethodOneLine(@NonNull Method method) {
        printAnnotationsOneLine(method.getDeclaredAnnotations());
        printMethodHeader(method);
    }

    private void printConstructors(@NonNull Class<?> someClass) {
        // sort constructors so the output is stable
        var constructors = someClass.getDeclaredConstructors();
        Arrays.sort(constructors, JavaDiff::compareConstructorsSignatureOnly);
        for (var constructor: constructors) {
            writer.println();
            printConstructor(constructor);
        }
    }

    private void printConstructor(@NonNull Constructor<?> constructor) {
        indent();
        printAnnotationsMultiLine(constructor.getDeclaredAnnotations());
        int modifiers = constructor.getModifiers();
        if (constructor.getDeclaringClass().isEnum()) {
            // all constructors in enums are private by default
            modifiers &= ~Modifier.PRIVATE;
        }
        if (constructor.getDeclaringClass().isInterface()) {
            modifiers &= ~Modifier.PUBLIC;
            modifiers &= ~Modifier.ABSTRACT;
        }
        if (constructor.isVarArgs()) {
            modifiers &= ~Modifier.TRANSIENT; // what were they even thinking?
        }
        printModifiers(modifiers);
        printTypeParameters(constructor.getTypeParameters());
        if (constructor.getTypeParameters().length > 0) {
            writer.print(" ");
        }
        writer.print(constructor.getDeclaringClass().getSimpleName());
        if (!Modifier.isStatic(constructor.getDeclaringClass().getModifiers()) &&
            constructor.getDeclaringClass().isMemberClass()) {
            int parameterCount = constructor.getParameters().length;
            printArgumentList(Arrays.copyOfRange(constructor.getParameters(),
                                                 min(parameterCount, 1),
                                                 parameterCount));
        } else if (constructor.getDeclaringClass().isEnum()) {
            // first two parameters in constructors in enums are implicit, ignore them
            int parameterCount = constructor.getParameters().length;
            printArgumentList(Arrays.copyOfRange(constructor.getParameters(),
                                                 min(parameterCount, 2),
                                                 parameterCount));
        } else {
            printArgumentList(constructor.getParameters());
        }
        printExceptions(constructor);
        writer.println(" { }");
    }

    private void printArgumentList(@NonNull Parameter[] parameters) {
        writer.print("(");
        boolean first = true;
        int position = 0;
        for (var parameter: parameters) {
            if (!first) {
                writer.print(", ");
            }
            printParameter(parameter, position++);
            first = false;
        }
        writer.print(")");
    }

    private void printParameter(@NonNull Parameter parameter, int position) {
        // unfortunately parameters in inner class' constructor for some reason have all generics erased
        // forbid these constructors to have parameters
        if ((parameter.getDeclaringExecutable() instanceof Constructor) &&
            !Modifier.isStatic(parameter.getDeclaringExecutable().getDeclaringClass().getModifiers()) &&
            parameter.getDeclaringExecutable().getDeclaringClass().isMemberClass()) {
            throw new IllegalArgumentException("constructors taking arguments in inner classes are forbidden");
        }
        printModifiers(parameter.getModifiers());
        if (parameter.isVarArgs()) {
            assert parameter.getAnnotatedType() instanceof AnnotatedArrayType;
            printAnnotatedType(((AnnotatedArrayType) parameter.getAnnotatedType()).getAnnotatedGenericComponentType());
            writer.print("...");
        } else {
            printAnnotatedType(parameter.getAnnotatedType());
        }
        writer.print(" ");
        printAnnotationsOneLine(parameter.getDeclaredAnnotations());
        writer.print("arg" + position);
    }

    private void printExceptions(@NonNull Executable executable) {
        if (executable.getAnnotatedExceptionTypes().length == 0) {
            return;
        }
        writer.print(" throws ");
        boolean first = true;
        for (var exceptionType: executable.getAnnotatedExceptionTypes()) {
            if (!first) {
                writer.print(", ");
            }
            printAnnotatedType(exceptionType);
            first = false;
        }
    }

    private void printBody(@NonNull Class<?> returnClass) {
        writer.println(" {");

        indent();
        writer.println("    return " + defaultValueForType(returnClass) + ";");
        indent();
        writer.println("}");
    }

    // Fields

    private void printFields(@NonNull Class<?> someClass) {
        // sort fields so the output is stable
        var fields = someClass.getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        boolean first = true;
        for (var field: fields) {
            if (field.isSynthetic() || field.isEnumConstant()) {
                continue;
            }
            if (first) {
                writer.println();
            }
            indent();
            printField(field);
            if (Modifier.isFinal(field.getModifiers())) {
                writer.print(" = " + defaultValueForType(field.getType()));
            }
            writer.println(";");
            first = false;
        }
    }

    void printField(@NonNull Field field) {
        printAnnotationsOneLine(field.getDeclaredAnnotations());
        printModifiers(field.getModifiers());
        printAnnotatedType(field.getAnnotatedType());
        writer.print(" " + field.getName());
    }

    private void printEnumConstants(@NonNull Class<?> someClass) {
        assert someClass.isEnum();

        // sort fields so the output is stable
        var fields = someClass.getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        boolean first = true;
        for (var field: fields) {
            if (!field.isEnumConstant()) {
                continue;
            }
            writer.println(first ? "" : ",");
            indent();
            printAnnotationsOneLine(field.getDeclaredAnnotations());
            writer.print(field.getName());

            first = false;
        }
        writer.println(";"); // can be omitted if nothing follows it, but meh
    }

    // Types

    private void printAnnotatedType(@NonNull AnnotatedType type) {
        if (type instanceof AnnotatedArrayType) {
            printAnnotatedArrayType((AnnotatedArrayType) type);
            return;
        }
        String name;
        if (type.getType() instanceof TypeVariable) {
            name = ((TypeVariable) type.getType()).getName();
        } else if (type instanceof AnnotatedWildcardType) {
            name = "?";
        } else if (type.getType() instanceof ParameterizedType) {
            assert ((ParameterizedType) type.getType()).getRawType() instanceof Class;
            var clazz = ((Class) ((ParameterizedType) type.getType()).getRawType());
            writer.print(getPackagePath(clazz) + ".");
            name = clazz.getSimpleName();
        } else {
            assert type.getType() instanceof Class;
            var clazz = (Class) type.getType();
            if (clazz.getPackage() != null) {
                writer.print(getPackagePath(clazz) + ".");
            }
            name = clazz.getSimpleName();
        }
        printAnnotationsOneLine(type.getDeclaredAnnotations());
        writer.print(name);
        if (type instanceof AnnotatedWildcardType) {
            printAnnotatedWildcardBounds((AnnotatedWildcardType) type);
        } else if (type instanceof AnnotatedParameterizedType) {
            printTypeArguments(((AnnotatedParameterizedType) type).getAnnotatedActualTypeArguments());
        }
    }

    private String getPackagePath(@NonNull Class<?> clazz) {
        return clazz.getEnclosingClass() == null ? clazz.getPackageName()
                                                 : clazz.getEnclosingClass().getCanonicalName();
    }

    private void printAnnotatedWildcardBounds(@NonNull AnnotatedWildcardType type) {
        // both loops can not be entered, so the order does not matter
        for (var bound: type.getAnnotatedUpperBounds()) {
            if (bound.getType() == Object.class) {
                continue;
            }
            writer.print(" extends ");
            printAnnotatedType(bound);
            // at most one iteration here
        }
        for (var bound: type.getAnnotatedLowerBounds()) {
            writer.print(" super ");
            printAnnotatedType(bound);
            // at most one iteration here
        }
    }

    private void printAnnotatedArrayType(@NonNull AnnotatedArrayType type) {
        printAnnotatedType(type.getAnnotatedGenericComponentType());
        if (type.getDeclaredAnnotations().length > 0) {
            writer.print(" ");
            printAnnotationsOneLine(type.getDeclaredAnnotations());
        }
        writer.print("[]");
    }

    private void printTypeArguments(@NonNull AnnotatedType[] typeParameters) {
        if (typeParameters.length == 0) {
            return;
        }
        writer.print("<");
        boolean first = true;
        for (var type: typeParameters) {
            if (!first) {
                writer.print(", ");
            }
            printAnnotatedType(type);
            first = false;
        }
        writer.print(">");
    }

    // Annotations

    private void printAnnotationsOneLine(@NonNull Annotation[] annotations) {
        for (var annotation: annotations) {
            printAnnotation(annotation);
            writer.print(" ");
        }
    }

    private void printAnnotationsMultiLine(@NonNull Annotation[] annotations) {
        for (var annotation: annotations) {
            indent();
            printAnnotation(annotation);
            writer.println();
        }
    }

    private void printAnnotation(@NonNull Annotation annotation) {
        writer.print(annotation.toString());
    }

    // Misc

    private String defaultValueForType(@NonNull Class<?> clazz) {
        assert clazz != void.class;

        var value = Array.get(Array.newInstance(clazz, 1), 0);
        return value == null ? "null" : value.toString();
    }
}
