package com.example.reflector;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

import static com.example.reflector.Reflector.diffClasses;

public class TestUtils {
    public static String diffClassesToString(@NonNull Class<?> classA, @NonNull Class<?> classB) {
        return outputFrom(printWriter -> diffClasses(classA, classB, printWriter));
    }

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
