package com.example.reflector;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

import static com.example.reflector.Reflector.diffClasses;

/** Utilities that are useful in all tests. */
public class TestUtils {
    /** Prints the diff between classes to a String and returns it. */
    public static String diffClassesToString(@NonNull Class<?> classA, @NonNull Class<?> classB) {
        return outputFrom(printWriter -> diffClasses(classA, classB, printWriter));
    }

    /** Runs the consumer, then prints to a PrintWriter, and returns the string printed. */
    public static String outputFrom(Consumer<PrintWriter> consumer) {
        String result;
        var stringWriter = new StringWriter();
        try (var printWriter = new PrintWriter(stringWriter)) {
            consumer.accept(printWriter);

            printWriter.flush();
            printWriter.close();
            result = stringWriter.toString();
        }
        return result;
    }
}
