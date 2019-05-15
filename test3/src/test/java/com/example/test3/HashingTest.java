package com.example.test3;

import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class HashingTest {
    @Test
    void testSingleThreadOneFile(@TempDir Path tempDir) {
        testOneFile(SingleThreadHashing::hash, tempDir);
    }

    @Test
    void testForkJoinOneFile(@TempDir Path tempDir) {
        testOneFile(ForkJoinHashing::hash, tempDir);
    }

    @Test
    void testSingleThreadNested(@TempDir Path tempDir) {
        testNested(SingleThreadHashing::hash, tempDir);
    }

    @Test
    void testForkJoinNested(@TempDir Path tempDir) {
        testNested(ForkJoinHashing::hash, tempDir);
    }

    void testOneFile(Function<@NonNull Path, byte[]> hash, @NonNull Path tempDir) {
        var testFile = tempDir.resolve("aaa");
        createFile(testFile, "aaaa");
        var result = hash.apply(testFile);
        System.out.println(Arrays.toString(result));
        assertArrayEquals(new byte[]{116, -72, 115, 55, 69, 66, 0, -44, -45, 63, -128, -60, 102, 61, -59, -27}, result);
    }

    void testNested(Function<@NonNull Path, byte[]> hash, @NonNull Path tempDir) {
        var testFile = tempDir.resolve("aaa");
        createFile(testFile, "aaaa");
        var directory = tempDir.resolve("bbb");
        directory.toFile().mkdirs();
        var testFile2 = directory.resolve("ccc");
        var testFile3 = directory.resolve("ddd");

        createFile(testFile2, "ooo");
        createFile(testFile3, "lll");

        var result = hash.apply(testFile);
        System.out.println(Arrays.toString(result));
        assertArrayEquals(new byte[]{116, -72, 115, 55, 69, 66, 0, -44, -45, 63, -128, -60, 102, 61, -59, -27}, result);
    }

    private void createFile(@NonNull Path path, @NonNull String content) {
        try {
            FileUtils.writeStringToFile(path.toFile(), content);
        } catch (IOException ignored) {
        }
    }
}