package com.example.reflector;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.AfterEach;

import java.io.OutputStream;
import java.lang.reflect.MalformedParametersException;
import java.util.Collection;

class ClassWithFields {
    int a;
    short b;
    long c;
}

class ClassWithConstructors {
    ClassWithConstructors() { }

    ClassWithConstructors(int a) { }

    ClassWithConstructors(String a, int b) { }
}

class ClassWithMethods {
    int a(int b) { return 0; }

    String c(String d) { return null; }

    void e(short f, short g) { }

    char h() { return 0; }

    boolean i() { return true; }
}

class ClassWithModifiers {
    private transient int a;
    protected volatile int b;
    int c;
    static int d;

    private ClassWithModifiers() { }

    protected ClassWithModifiers(int a) { }

    public static void f(final int a) { }

    public native String f();
}

class ClassWithTypeParameters<U> {
    <T> T a(T b) { return null; }

    <K> void b(K a, U b) { }
}

class ClassWithExceptions {
    void a() throws Exception { }

    void b() throws IllegalArgumentException, Exception, NullPointerException { }

    ClassWithExceptions() throws UnsupportedOperationException, MalformedParametersException { }
}

class ClassWithGenerics {
    <T> T c(Class<T> d, T e) { return null; }

    <U> ClassWithGenerics(U a) { }

    <U extends StringBuilder> ClassWithGenerics(U a) { }
}

class ClassWithBadConstructor {
    class Inner {
        <T> Inner(T a) { }
    }
}

class ClassWithFinalFields {
    final int a = 10;
    final double b = 11;
    final String c = "123";
}

abstract class ClassWithAbstractMethods {
    abstract void a();

    abstract String b(int o);
}

class ClassWithInterface {
    private interface I1 {
        default int a() {
            return 11;
        }

        String b(int o);
    }
}

class ClassWithGenericSubclass {
    private class B1<T, U> {
        T a;
        U b;
    }
}

class ClassWithGenericTypeExtends {
    <P, O extends Class<P>> ClassWithGenericTypeExtends(O o) { }
}

class ClassWithTypeMultipleExtends {
    <P, O extends I1 & I2> ClassWithTypeMultipleExtends(O o) { }

    interface I1 {}

    interface I2 {}
}

class ClassWithWildcards {
    <T, R> void a(Collection<? extends T> b, Collection<? super R> c) {

    }
}

@Nullable
class ClassWithAnnotations extends @Nullable Object {
    <@NonNull T extends @Nullable OutputStream> ClassWithAnnotations(@Nullable T @NonNull [] a) { }

    @NonNull
    final String a = null;

    @AfterEach
    <T> @Nullable Object m(@NonNull T o, Collection<@NonNull ? extends @Nullable Class> c) {
        return null;
    }

    @Deprecated
    ClassWithAnnotations() { }
}

class ClassWithInnerEnum {
    enum E {
        A,
        B,
        C;

        E() { }

        private E(int a, String b) { }
    }
}