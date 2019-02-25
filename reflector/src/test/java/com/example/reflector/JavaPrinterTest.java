package com.example.reflector;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import static com.example.reflector.TestUtils.outputFrom;
import static org.junit.jupiter.api.Assertions.*;

/** Tests here compile classes and check that the output is as expected */
class JavaPrinterTest {
    @Test
    void testClassWithFields() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithFields {\n" +
                             "    int a;\n" +
                             "    short b;\n" +
                             "    long c;\n" +
                             "\n" +
                             "    ClassWithFields() { }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithFields.class));
    }

    @Test
    void testClassWithConstructors() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithConstructors {\n" +
                             "    ClassWithConstructors() { }\n" +
                             "\n" +
                             "    ClassWithConstructors(int arg0) { }\n" +
                             "\n" +
                             "    ClassWithConstructors(java.lang.String arg0, int arg1) { }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithConstructors.class));
    }

    @Test
    void testClassWithMethods() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithMethods {\n" +
                             "    ClassWithMethods() { }\n" +
                             "\n" +
                             "    int a(int arg0) {\n" +
                             "        return 0;\n" +
                             "    }\n" +
                             "\n" +
                             "    java.lang.String c(java.lang.String arg0) {\n" +
                             "        return null;\n" +
                             "    }\n" +
                             "\n" +
                             "    void e(short arg0, short arg1) { }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithMethods.class));
    }

    @Test
    void testClassWithModifiers() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithVariousModifiers {\n" +
                             "    private int a;\n" +
                             "    protected int b;\n" +
                             "    int c;\n" +
                             "    static int d;\n" +
                             "\n" +
                             "    private ClassWithVariousModifiers() { }\n" +
                             "\n" +
                             "    protected ClassWithVariousModifiers(int arg0) { }\n" +
                             "\n" +
                             "    public static void f(int arg0) { }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithVariousModifiers.class));
    }

    @Test
    void testClassWithTypeParameters() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithTypeParameters<U> {\n" +
                             "    ClassWithTypeParameters() { }\n" +
                             "\n" +
                             "    <T> T a(T arg0) {\n" +
                             "        return null;\n" +
                             "    }\n" +
                             "\n" +
                             "    <K> void b(K arg0, U arg1) { }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithTypeParameters.class));
    }

    @Test
    void testClassWithExceptions() {
        var expectedOutput =
                "package com.example.reflector;\n" +
                "\n" +
                "public class ClassWithExceptions {\n" +
                "    ClassWithExceptions() throws java.lang.UnsupportedOperationException, java.lang.reflect.MalformedParametersException { }\n" +
                "\n" +
                "    void a() throws java.lang.Exception { }\n" +
                "\n" +
                "    void b() throws java.lang.IllegalArgumentException, java.lang.Exception, java.lang.NullPointerException { }\n" +
                "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithExceptions.class));
    }

    @Test
    void testClassWithGenerics() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithGenerics {\n" +
                             "    <U> ClassWithGenerics(U arg0) { }\n" +
                             "\n" +
                             "    <U extends java.lang.StringBuilder> ClassWithGenerics(U arg0) { }\n" +
                             "\n" +
                             "    <T> T c(java.lang.Class<T> arg0, T arg1) {\n" +
                             "        return null;\n" +
                             "    }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithGenerics.class));
    }

    @Test
    void testClassWithBadConstructor() {
        assertThrows(IllegalArgumentException.class, () -> printStructureToString(ClassWithBadConstructor.class));
    }

    @Test
    void testClassWithFinalFields() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithFinalFields {\n" +
                             "    final int a = 0;\n" +
                             "    final double b = 0.0;\n" +
                             "    final java.lang.String c = null;\n" +
                             "\n" +
                             "    ClassWithFinalFields() { }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithFinalFields.class));
    }

    @Test
    void testClassWithAbstractMethods() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public abstract class ClassWithAbstractMethods {\n" +
                             "    ClassWithAbstractMethods() { }\n" +
                             "\n" +
                             "    abstract void a();\n" +
                             "\n" +
                             "    abstract java.lang.String b(int arg0);\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithAbstractMethods.class));
    }

    @Test
    void testClassWithInterface() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithInterface {\n" +
                             "    ClassWithInterface() { }\n" +
                             "\n" +
                             "    private interface I1 {\n" +
                             "        default int a() {\n" +
                             "            return 0;\n" +
                             "        }\n" +
                             "\n" +
                             "        java.lang.String b(int arg0);\n" +
                             "    }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithInterface.class));
    }

    @Test
    void testClassWithGenericSubclass() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithGenericSubclass {\n" +
                             "    ClassWithGenericSubclass() { }\n" +
                             "\n" +
                             "    private class B1<T, U> {\n" +
                             "        T a;\n" +
                             "        U b;\n" +
                             "\n" +
                             "        private B1() { }\n" +
                             "    }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithGenericSubclass.class));
    }

    @Test
    void testClassWithGenericTypeExtends() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithGenericTypeExtends {\n" +
                             "    <P, O extends java.lang.Class<P>> ClassWithGenericTypeExtends(O arg0) { }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithGenericTypeExtends.class));
    }

    @Test
    void testClassWithMultipleExtends() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithTypeMultipleExtends {\n" +
                             "    <P, O extends com.example.reflector.ClassWithTypeMultipleExtends.I1 & com.example.reflector.ClassWithTypeMultipleExtends.I2> ClassWithTypeMultipleExtends(O arg0) { }\n" +
                             "\n" +
                             "    interface I1 { }\n" +
                             "\n" +
                             "    interface I2 { }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithTypeMultipleExtends.class));
    }

    @Test
    void testClassWithWildcards() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithWildcards {\n" +
                             "    ClassWithWildcards() { }\n" +
                             "\n" +
                             "    <T, R> void a(java.util.Collection<? extends T> arg0, java.util.Collection<? super R> arg1) { }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithWildcards.class));
    }

    @Test
    void testClassWithAnnotations() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "@org.checkerframework.checker.nullness.qual.Nullable()\n" +
                             "public class ClassWithAnnotations extends java.lang.@org.checkerframework.checker.nullness.qual.Nullable() Object {\n" +
                             "    final java.lang.@org.checkerframework.checker.nullness.qual.NonNull() String a = null;\n" +
                             "\n" +
                             "    <@org.checkerframework.checker.nullness.qual.NonNull() T extends java.io.@org.checkerframework.checker.nullness.qual.Nullable() OutputStream> ClassWithAnnotations(@org.checkerframework.checker.nullness.qual.Nullable() T @org.checkerframework.checker.nullness.qual.NonNull() [] arg0) { }\n" +
                             "\n" +
                             "    @org.junit.jupiter.api.AfterEach()\n" +
                             "    <T> java.lang.@org.checkerframework.checker.nullness.qual.Nullable() Object m(@org.checkerframework.checker.nullness.qual.NonNull() T arg0, java.util.Collection<@org.checkerframework.checker.nullness.qual.NonNull() ? extends java.lang.@org.checkerframework.checker.nullness.qual.Nullable() Class> arg1) {\n" +
                             "        return null;\n" +
                             "    }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithAnnotations.class));
    }

    @Test
    void testClassWithInnerEnum() {
        var expectedOutput = "package com.example.reflector;\n" +
                             "\n" +
                             "public class ClassWithInnerEnum {\n" +
                             "    ClassWithInnerEnum() { }\n" +
                             "\n" +
                             "    enum E {\n" +
                             "        A,\n" +
                             "        B,\n" +
                             "        C;\n" +
                             "\n" +
                             "        E() { }\n" +
                             "\n" +
                             "        E(int arg0, java.lang.String arg1) { }\n" +
                             "    }\n" +
                             "}\n";
        assertEquals(expectedOutput, printStructureToString(ClassWithInnerEnum.class));
    }

    @Test
    void testExceptions() {
        assertThrows(NullPointerException.class, () -> new JavaPrinter(null));
    }

    private String printStructureToString(@NonNull Class<?> clazz) {
        return outputFrom((writer) -> Reflector.printStructure(clazz, writer));
    }
}