package com.example.reflector;

import org.checkerframework.checker.nullness.qual.*;
import java.io.*;

import org.apache.commons.lang3.Validate;

/** Reflections utilities, includin class printer and comparator. */
public class Reflector {
    /**
     * Prints a class with structure of {@code someClass} to a file named {@code someClass.getName() + ".java"} in
     * the current directory.
     */
    public static void printStructure(@NonNull Class<?> someClass) throws IOException {
        Validate.notNull(someClass, "someClass can not be null");

        var outputFile = new File(someClass.getName() + ".java");
        assert outputFile.createNewFile();
        var outputWriter = new PrintWriter(outputFile);
        printStructure(someClass, outputWriter);
        outputWriter.flush();
        outputWriter.close();
    }

    /** Prints a class with structure of {@code someClass} to {@code writer}. */
    public static void printStructure(@NonNull Class<?> someClass, @NonNull PrintWriter writer) {
        Validate.notNull(someClass, "someClass can not be null");
        Validate.notNull(writer, "writer can not be null");

        var javaPrinter = new JavaPrinter(writer);
        javaPrinter.printClass(someClass);
    }

    /** Prints differences in classes' fields and methods to standard output. */
    public static void diffClasses(@NonNull Class<?> classA,
                                   @NonNull Class<?> classB) {
        Validate.notNull(classA, "classA can not be null");
        Validate.notNull(classB, "classB can not be null");

        var writer = new PrintWriter(System.out);
        diffClasses(classA, classB, writer);
        writer.flush();
        writer.close();
    }

    /** Prints differences in classes' fields and methods to {@code writer}. */
    public static void diffClasses(@NonNull Class<?> classA,
                                   @NonNull Class<?> classB,
                                   @NonNull PrintWriter writer) {
        Validate.notNull(classA, "classA can not be null");
        Validate.notNull(classB, "classB can not be null");
        Validate.notNull(writer, "writer can not be null");

        var javaComparator = new JavaDiff(writer);
        javaComparator.diffClasses(classA, classB);
    }
}
