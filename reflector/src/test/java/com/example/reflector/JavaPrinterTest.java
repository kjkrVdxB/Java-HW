package com.example.reflector;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import static com.example.reflector.TestUtils.outputFrom;
import static org.junit.jupiter.api.Assertions.*;

/** Tests here compile classes and check that the output is as expected */
class JavaPrinterTest {
    private final static String NEWLINE = System.lineSeparator();

    @Test
    void testClassWithFields() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithFields {" + NEWLINE +
                             "    int a;" + NEWLINE +
                             "    short b;" + NEWLINE +
                             "    long c;" + NEWLINE +
                             "" + NEWLINE +
                             "    ClassWithFields() { }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithFields.class));
    }

    @Test
    void testClassWithConstructors() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithConstructors {" + NEWLINE +
                             "    ClassWithConstructors() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    ClassWithConstructors(int arg0) { }" + NEWLINE +
                             "" + NEWLINE +
                             "    ClassWithConstructors(java.lang.String arg0, int arg1) { }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithConstructors.class));
    }

    @Test
    void testClassWithMethods() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithMethods {" + NEWLINE +
                             "    ClassWithMethods() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    int a(int arg0) {" + NEWLINE +
                             "        return 0;" + NEWLINE +
                             "    }" + NEWLINE +
                             "" + NEWLINE +
                             "    java.lang.String c(java.lang.String arg0) {" + NEWLINE +
                             "        return null;" + NEWLINE +
                             "    }" + NEWLINE +
                             "" + NEWLINE +
                             "    void e(short arg0, short arg1) { }" + NEWLINE +
                             "" + NEWLINE +
                             "    char h() {" + NEWLINE +
                             "        return ' ';" + NEWLINE +
                             "    }" + NEWLINE +
                             "" + NEWLINE +
                             "    boolean i() {" + NEWLINE +
                             "        return false;" + NEWLINE +
                             "    }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithMethods.class));
    }

    @Test
    void testClassWithModifiers() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithModifiers {" + NEWLINE +
                             "    private transient int a;" + NEWLINE +
                             "    protected volatile int b;" + NEWLINE +
                             "    int c;" + NEWLINE +
                             "    static int d;" + NEWLINE +
                             "" + NEWLINE +
                             "    private ClassWithModifiers() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    protected ClassWithModifiers(int arg0) { }" + NEWLINE +
                             "" + NEWLINE +
                             "    public native java.lang.String f();" + NEWLINE +
                             "" + NEWLINE +
                             "    public static void f(int arg0) { }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithModifiers.class));
    }

    @Test
    void testClassWithTypeParameters() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithTypeParameters<U> {" + NEWLINE +
                             "    ClassWithTypeParameters() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    <T> T a(T arg0) {" + NEWLINE +
                             "        return null;" + NEWLINE +
                             "    }" + NEWLINE +
                             "" + NEWLINE +
                             "    <K> void b(K arg0, U arg1) { }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithTypeParameters.class));
    }

    @Test
    void testClassWithExceptions() {
        var expectedOutput =
                "package com.example.reflector;" + NEWLINE +
                "" + NEWLINE +
                "public class ClassWithExceptions {" + NEWLINE +
                "    ClassWithExceptions() throws java.lang.UnsupportedOperationException, java.lang.reflect.MalformedParametersException { }" + NEWLINE +
                "" + NEWLINE +
                "    void a() throws java.lang.Exception { }" + NEWLINE +
                "" + NEWLINE +
                "    void b() throws java.lang.IllegalArgumentException, java.lang.Exception, java.lang.NullPointerException { }" + NEWLINE +
                "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithExceptions.class));
    }

    @Test
    void testClassWithGenerics() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithGenerics {" + NEWLINE +
                             "    <U> ClassWithGenerics(U arg0) { }" + NEWLINE +
                             "" + NEWLINE +
                             "    <U extends java.lang.StringBuilder> ClassWithGenerics(U arg0) { }" + NEWLINE +
                             "" + NEWLINE +
                             "    <T> T c(java.lang.Class<T> arg0, T arg1) {" + NEWLINE +
                             "        return null;" + NEWLINE +
                             "    }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithGenerics.class));
    }

    @Test
    void testClassWithBadConstructor() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithBadConstructor {" + NEWLINE +
                             "    ClassWithBadConstructor() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    class Inner {}" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithBadConstructor.class));
    }

    @Test
    void testClassWithFinalFields() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithFinalFields {" + NEWLINE +
                             "    final int a = 0;" + NEWLINE +
                             "    final double b = 0.0;" + NEWLINE +
                             "    final java.lang.String c = null;" + NEWLINE +
                             "" + NEWLINE +
                             "    ClassWithFinalFields() { }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithFinalFields.class));
    }

    @Test
    void testClassWithAbstractMethods() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public abstract class ClassWithAbstractMethods {" + NEWLINE +
                             "    ClassWithAbstractMethods() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    abstract void a();" + NEWLINE +
                             "" + NEWLINE +
                             "    abstract java.lang.String b(int arg0);" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithAbstractMethods.class));
    }

    @Test
    void testClassWithInterface() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithInterface {" + NEWLINE +
                             "    ClassWithInterface() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    private interface I1 {" + NEWLINE +
                             "        default int a() {" + NEWLINE +
                             "            return 0;" + NEWLINE +
                             "        }" + NEWLINE +
                             "" + NEWLINE +
                             "        java.lang.String b(int arg0);" + NEWLINE +
                             "    }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithInterface.class));
    }

    @Test
    void testClassWithGenericSubclass() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithGenericSubclass {" + NEWLINE +
                             "    ClassWithGenericSubclass() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    private class B1<T, U> {" + NEWLINE +
                             "        T a;" + NEWLINE +
                             "        U b;" + NEWLINE +
                             "    }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithGenericSubclass.class));
    }

    @Test
    void testClassWithExtendsImplements() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithExtendsImplements extends java.io.OutputStream implements java.io.Serializable, java.lang.reflect.Type {" + NEWLINE +
                             "    ClassWithExtendsImplements() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    public void write(int arg0) throws java.io.IOException { }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithExtendsImplements.class));
    }

    @Test
    void testClassWithGenericTypeExtends() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithGenericTypeExtends {" + NEWLINE +
                             "    <P, O extends java.lang.Class<P>> ClassWithGenericTypeExtends(O arg0) { }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithGenericTypeExtends.class));
    }

    @Test
    void testClassWithMultipleExtends() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithTypeMultipleExtends {" + NEWLINE +
                             "    <P, O extends com.example.reflector.ClassWithTypeMultipleExtends.I1 & com.example.reflector.ClassWithTypeMultipleExtends.I2> ClassWithTypeMultipleExtends(O arg0) { }" + NEWLINE +
                             "" + NEWLINE +
                             "    interface I1 {}" + NEWLINE +
                             "" + NEWLINE +
                             "    interface I2 {}" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithTypeMultipleExtends.class));
    }

    @Test
    void testClassWithWildcards() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithWildcards {" + NEWLINE +
                             "    ClassWithWildcards() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    <T, R> void a(java.util.Collection<? extends T> arg0, java.util.Collection<? super R> arg1) { }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithWildcards.class));
    }

    @Test
    void testClassWithAnnotations() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "@org.checkerframework.checker.nullness.qual.Nullable()" + NEWLINE +
                             "public class ClassWithAnnotations extends java.lang.@org.checkerframework.checker.nullness.qual.Nullable() Object {" + NEWLINE +
                             "    final java.lang.@org.checkerframework.checker.nullness.qual.NonNull() String a = null;" + NEWLINE +
                             "" + NEWLINE +
                             "    @java.lang.Deprecated(forRemoval=false, since=\"\")" + NEWLINE +
                             "    ClassWithAnnotations() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    <@org.checkerframework.checker.nullness.qual.NonNull() T extends java.io.@org.checkerframework.checker.nullness.qual.Nullable() OutputStream> ClassWithAnnotations(@org.checkerframework.checker.nullness.qual.Nullable() T @org.checkerframework.checker.nullness.qual.NonNull() [] arg0) { }" + NEWLINE +
                             "" + NEWLINE +
                             "    @org.junit.jupiter.api.AfterEach()" + NEWLINE +
                             "    <T> java.lang.@org.checkerframework.checker.nullness.qual.Nullable() Object m(@org.checkerframework.checker.nullness.qual.NonNull() T arg0, java.util.Collection<@org.checkerframework.checker.nullness.qual.NonNull() ? extends java.lang.@org.checkerframework.checker.nullness.qual.Nullable() Class> arg1) {" + NEWLINE +
                             "        return null;" + NEWLINE +
                             "    }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithAnnotations.class));
    }

    @Test
    void testClassWithInnerEnum() {
        var expectedOutput = "package com.example.reflector;" + NEWLINE +
                             "" + NEWLINE +
                             "public class ClassWithInnerEnum {" + NEWLINE +
                             "    ClassWithInnerEnum() { }" + NEWLINE +
                             "" + NEWLINE +
                             "    enum E {" + NEWLINE +
                             "        A," + NEWLINE +
                             "        B," + NEWLINE +
                             "        C;" + NEWLINE +
                             "" + NEWLINE +
                             "        E() { }" + NEWLINE +
                             "" + NEWLINE +
                             "        E(int arg0, java.lang.String arg1) { }" + NEWLINE +
                             "    }" + NEWLINE +
                             "}" + NEWLINE;
        assertEquals(expectedOutput, printStructureToString(ClassWithInnerEnum.class));
    }

    @Test
    void testNullClass() {
        assertThrows(NullPointerException.class, () -> new JavaPrinter(null));
    }

    class ClassNotOnTopLevel {

    }

    @Test
    void testClassNotOnTopLevel() {
        assertThrows(IllegalArgumentException.class, () -> printStructureToString(ClassNotOnTopLevel.class));
    }

    private String printStructureToString(@NonNull Class<?> clazz) {
        return outputFrom((writer) -> Reflector.printStructure(clazz, writer));
    }
}