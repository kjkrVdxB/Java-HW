package com.example.reflector;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

import static com.example.reflector.TestUtils.diffClassesToString;
import static org.junit.jupiter.api.Assertions.*;

class JavaDiffTest {
    class A1 {
        short a;
        double b;
        int c;
    }

    class B1 {
        int a;
        String b;
        int c;
    }

    @Test
    void testDiffBasicFields() {
        var expectedDiff = "< short a\n" +
                           "> int a\n" +
                           "< double b\n" +
                           "> java.lang.String b\n";
        assertEquals(expectedDiff, diffClassesToString(A1.class, B1.class));
    }

    class A2 {
        int a;
        short b;
    }

    class B2 {
        int c;
        short d;
    }

    @Test
    void testDiffFieldsWithoutCounterpart() {
        var expectedDiff = "< int a\n" +
                           "< short b\n" +
                           "> int c\n" +
                           "> short d\n";
        assertEquals(expectedDiff, diffClassesToString(A2.class, B2.class));
    }

    class A3 {
        int a(int b) {
            return 0;
        }

        String c(short d) {
            return null;
        }

        int k(int u) {
            return 0;
        }
    }

    class B3 {
        int a(int b) {
            return 0;
        }

        String c(short d) {
            return null;
        }

        int k(short u) {
            return 0;
        }
    }

    @Test
    void testDiffBasicMethods() {
        var expectedDiff = "< int k(int arg0)\n" +
                           "> int k(short arg0)\n";
        assertEquals(expectedDiff, diffClassesToString(A3.class, B3.class));
    }

    class A4 {
        public int a;
        public int b;
        private int c;
        private int d;
        protected int e;
        int f;
        public int g;
    }

    class B4 {
        private int a;
        int b;
        protected int c;
        private int d;
        protected int e;
        int f;
        public int g;
    }

    @Test
    void testDiffFieldsAccessModifiers() {
        var expectedDiff = "< public int a\n" +
                           "> private int a\n" +
                           "< public int b\n" +
                           "> int b\n" +
                           "< private int c\n" +
                           "> protected int c\n";
        assertEquals(expectedDiff, diffClassesToString(A4.class, B4.class));
    }

    class A5 {
        public void a() { }
        public void b() { }
        private void c() { }
        private void d() { }
        protected void e() { }
        void f() { }
        public void g() { }
    }

    class B5 {
        private void a() { }
        void b() { }
        protected void c() { }
        private void d() { }
        protected void e() { }
        void f() { }
        public void g() { }
    }

    @Test
    void testDiffMethodsAccessModifiers() {
        var expectedDiff = "< public void a()\n" +
                           "> private void a()\n" +
                           "< public void b()\n" +
                           "> void b()\n" +
                           "< private void c()\n" +
                           "> protected void c()\n";
        assertEquals(expectedDiff, diffClassesToString(A5.class, B5.class));
    }

    class A6 {
        <T> void a() { }
        <U> void b(U k) { }
        <T extends A1> void c() { }
        <T extends A1> void d() { }
    }

    class B6 {
        <T> void a() { }
        <K> void b(K k) { }
        <T extends A1> void c() { }
        <T extends B1> void d() { }
    }

    @Test
    void testDiffGenericMethods() {
        var expectedDiff = "< <U> void b(U arg0)\n" +
                           "> <K> void b(K arg0)\n" +
                           "< <T extends com.example.reflector.JavaDiffTest.A1> void d()\n" +
                           "> <T extends com.example.reflector.JavaDiffTest.B1> void d()\n";
        assertEquals(expectedDiff, diffClassesToString(A6.class, B6.class));
    }

    class A7 {
        <T> void a(T b) { }
    }

    class B7 {
        <T extends Object> void a(T a) { }
    }

    @Test
    void testDiffExtendsObject() {
        assertEquals("", diffClassesToString(A7.class, B7.class));
    }

    class A8 {
        @Nullable @NonNull String a;
        @Nullable @NonNull String b;
        @NonNull String c;

        void k(Collection<? extends @Nullable OutputStream> p) { }
        void l(Collection<? super @Nullable OutputStream> p) { }
        void p(@NonNull Integer a) { }
        <@NonNull T> void r() { }
        @NonNull Integer t() { return 1; }
    }

    class B8 {
        @NonNull @Nullable String a;
        @Nullable @NonNull String b;
        @Nullable String c;

        void k(Collection<? extends @NonNull OutputStream> p) { }
        void l(Collection<? super @NonNull OutputStream> p) { }
        void p(Integer a) { }
        <T> void r() { }
        Integer t() { return null; }
    }

    @Test
    void testDiffAnnotations() {
        var expectedDiff = "< java.lang.@org.checkerframework.checker.nullness.qual.NonNull() String c\n" +
                           "> java.lang.@org.checkerframework.checker.nullness.qual.Nullable() String c\n" +
                           "< void k(java.util.Collection<? extends java.io.@org.checkerframework.checker.nullness.qual.Nullable() OutputStream> arg0)\n" +
                           "> void k(java.util.Collection<? extends java.io.@org.checkerframework.checker.nullness.qual.NonNull() OutputStream> arg0)\n" +
                           "< void l(java.util.Collection<? super java.io.@org.checkerframework.checker.nullness.qual.Nullable() OutputStream> arg0)\n" +
                           "> void l(java.util.Collection<? super java.io.@org.checkerframework.checker.nullness.qual.NonNull() OutputStream> arg0)\n" +
                           "< void p(java.lang.@org.checkerframework.checker.nullness.qual.NonNull() Integer arg0)\n" +
                           "> void p(java.lang.Integer arg0)\n" +
                           "< <@org.checkerframework.checker.nullness.qual.NonNull() T> void r()\n" +
                           "> <T> void r()\n" +
                           "< java.lang.@org.checkerframework.checker.nullness.qual.NonNull() Integer t()\n" +
                           "> java.lang.Integer t()\n";
        assertEquals(expectedDiff, diffClassesToString(A8.class, B8.class));
    }

    class A9 {
        void a() throws Exception, IllegalArgumentException, NullPointerException { }
        void b() throws Exception, IllegalArgumentException, NullPointerException { }
    }

    class B9 {
        void a() throws NullPointerException, Exception, IllegalArgumentException { }
        void b() throws Exception, IllegalArgumentException { }
    }

    @Test
    void testDiffExceptions() {
        var expectedDiff = "< void b() throws java.lang.Exception, java.lang.IllegalArgumentException, java.lang.NullPointerException\n" +
                           "> void b() throws java.lang.Exception, java.lang.IllegalArgumentException\n";
        assertEquals(expectedDiff, diffClassesToString(A9.class, B9.class));
    }

    class A10 {
        <T> void a(Class<T> b) { }
        <T> void a(Class<?>[] b) { }
    }

    class B10 {
        <T> void a(Class<?> b) { }
        <T> void a(T b) { }
    }

    @Test
    void testDiffParameterTypes() {
        var expectedDiff = "< <T> void a(java.lang.Class<T> arg0)\n" +
                           "> <T> void a(java.lang.Class<?> arg0)\n" +
                           "< <T> void a(java.lang.Class<?>[] arg0)\n" +
                           "> <T> void a(T arg0)\n";
        assertEquals(expectedDiff, diffClassesToString(A10.class, B10.class));
    }

    @Test
    void testSameClasses() {
        assertEquals("", diffClassesToString(A1.class, A1.class));
        assertEquals("", diffClassesToString(B1.class, B1.class));
        assertEquals("", diffClassesToString(A2.class, A2.class));
        assertEquals("", diffClassesToString(B2.class, B2.class));
        assertEquals("", diffClassesToString(A3.class, A3.class));
        assertEquals("", diffClassesToString(B3.class, B3.class));
        assertEquals("", diffClassesToString(A4.class, A4.class));
        assertEquals("", diffClassesToString(B4.class, B4.class));
        assertEquals("", diffClassesToString(A5.class, A5.class));
        assertEquals("", diffClassesToString(B5.class, B5.class));
        assertEquals("", diffClassesToString(A6.class, A6.class));
        assertEquals("", diffClassesToString(B6.class, B6.class));
        assertEquals("", diffClassesToString(A7.class, A7.class));
        assertEquals("", diffClassesToString(B7.class, B7.class));
        assertEquals("", diffClassesToString(A8.class, A8.class));
        assertEquals("", diffClassesToString(B8.class, B8.class));
        assertEquals("", diffClassesToString(A9.class, A9.class));
        assertEquals("", diffClassesToString(B9.class, B9.class));
        assertEquals("", diffClassesToString(A10.class, A10.class));
        assertEquals("", diffClassesToString(B10.class, B10.class));
    }

    @Test
    void testExceptions() {
        assertThrows(NullPointerException.class, () -> new JavaDiff(null));
        var javaDiff = new JavaDiff(new PrintWriter(Writer.nullWriter()));
        assertThrows(NullPointerException.class, () -> javaDiff.diffClasses(null, B1.class));
        assertThrows(NullPointerException.class, () -> javaDiff.diffClasses(A1.class, null));
    }
}