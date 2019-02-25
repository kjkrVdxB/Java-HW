package com.example.reflector;

import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.List;

import static com.example.reflector.TestUtils.diffClassesToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Here are only combined print-compare tests. */
class ReflectorTest {
    @Test
    void testClassWithFields() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithFields.class);
    }

    @Test
    void testClassWithConstructors() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithConstructors.class);
    }

    @Test
    void testClassWithMethods() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithMethods.class);
    }

    @Test
    void testClassWithModifiers() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithVariousModifiers.class);
    }

    @Test
    void testClassWithTypeParameters() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithTypeParameters.class);
    }

    @Test
    void testClassWithExceptions() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithExceptions.class);
    }

    @Test
    void testClassWithGenerics() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithGenerics.class);
    }

    @Test
    void testClassWithFinalFields() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithFinalFields.class);
    }

    @Test
    void testClassWithAbstractMethods() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithAbstractMethods.class);
    }

    @Test
    void testClassWithInterface() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithInterface.class);
    }

    @Test
    void testClassWithGenericSubclass() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithGenericSubclass.class);
    }

    @Test
    void testClassWithGenericTypeExtends() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithGenericTypeExtends.class);
    }

    @Test
    void testClassWithMultipleExtends() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithTypeMultipleExtends.class);
    }

    @Test
    void testClassWithWildcards() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithWildcards.class);
    }

    @Test
    void testClassWithAnnotations() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithAnnotations.class);
    }

    @Test
    void testClassWithInnerEnum() throws IOException, ClassNotFoundException {
        runPrintCompareTest(ClassWithInnerEnum.class);
    }

    private void runPrintCompareTest(@NonNull Class<?> clazz) throws ClassNotFoundException, IOException {
        final var compilationUnit = clazz.getCanonicalName();

        var tempDirectory = Files.createTempDirectory("reflectorTest");
        tempDirectory = tempDirectory.toAbsolutePath();
        System.out.println(tempDirectory.toString());
        var fullPath = tempDirectory.toAbsolutePath().toString() +
                       File.separator +
                       compilationUnit.replace(".", File.separator) + ".java";
        var file = new File(fullPath);
        assertTrue(file.getParentFile().mkdirs());
        assertTrue(file.createNewFile());

        try (var writer = new PrintWriter(file)) {
            Reflector.printStructure(clazz, writer);
            writer.flush();
        }

        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = compiler.getStandardFileManager(null, null, null);

        var javaSources = fileManager.getJavaFileObjectsFromFiles(List.of(file));

        compiler.getTask(null, fileManager, null, null, null, javaSources).call();

        var classLoader = new URLClassLoader(new URL[]{tempDirectory.toUri().toURL()},
                                             new FirewallClassLoader(ClassLoader.getSystemClassLoader(),
                                                                     compilationUnit));
        var compiledClass = classLoader.loadClass(compilationUnit);

        assert compiledClass != clazz;

        assertEquals("", diffClassesToString(clazz, compiledClass));

        FileUtils.deleteDirectory(tempDirectory.toFile());
    }

    static class FirewallClassLoader extends ClassLoader {
        String blockedCompilationUnit;

        public FirewallClassLoader(ClassLoader parent, String blockedCompilationUnit) {
            super(parent);
            this.blockedCompilationUnit = blockedCompilationUnit;
        }

        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.equals(blockedCompilationUnit)
                || name.startsWith(blockedCompilationUnit + "$")) {
                throw new ClassNotFoundException();
            }
            return super.loadClass(name, resolve);
        }
    }
}
