package com.example.p2cw4;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class FTPTest {
    FTPServer server = new FTPServer(9999);
    FTPClient client = new FTPClient();

    FTPTest() throws IOException {}

    @BeforeEach
    void startServer() {
        server.start();
    }

    @BeforeEach
    void startClient() throws IOException {
        client.connect("localhost", 9999);
    }

    @AfterEach
    void stopServer() throws InterruptedException, IOException {
        server.stop();
    }

    @AfterEach
    void stopClient() throws IOException {
        client.disconnect();
    }

    @Test
    void testListBasic(@TempDir Path tmpDir) throws IOException {
        tmpDir.resolve("aaa").toFile().createNewFile();
        tmpDir.resolve("bbb").toFile().mkdirs();
        tmpDir.resolve("bbb").resolve("ccc").toFile().createNewFile();

        var list = client.executeList(tmpDir.toString());
        list.sort(Comparator.comparing(a -> a.left));

        var listExpected = Arrays.asList(
                new ImmutablePair<>("aaa", false),
                new ImmutablePair<>("bbb", true));

        assertEquals(listExpected, list);
    }

    @Test
    void testEmptyList(@TempDir Path tmpDir) throws IOException {
        var list = client.executeList(tmpDir.toString());
        list.sort(Comparator.comparing(a -> a.left));
        assertEquals(0, list.size());
    }

    @Test
    void testListSubdirectory(@TempDir Path tmpDir) throws IOException {
        // aaa -> bbb -> ccc -----  ddd(dir)
        //                    | --  eee(file)

        var path = tmpDir.resolve("aaa").resolve("bbb").resolve("ccc");

        path.resolve("ddd").toFile().mkdirs();
        path.resolve("eee").toFile().createNewFile();

        var list = client.executeList(path.toString());
        list.sort(Comparator.comparing(a -> a.left));

        var listExpected = Arrays.asList(
                new ImmutablePair<>("ddd", true),
                new ImmutablePair<>("eee", false));

        assertEquals(listExpected, list);
    }

    @Test
    void testGetBasic(@TempDir Path tmpDir) throws IOException {
        var filePath = tmpDir.resolve("test");
        var arrayExpected = new byte[]{1, 2, 3, 4, 5};

        filePath.toFile().createNewFile();

        try (var output = Files.newOutputStream(filePath)) {
            output.write(arrayExpected);
            output.flush();
        }

        var byteArray = client.executeGet(filePath.toString());
        assertArrayEquals(arrayExpected, byteArray);
    }

    @Test
    void testGetEmpty(@TempDir Path tmpDir) throws IOException {
        var filePath = tmpDir.resolve("test");
        var arrayExpected = new byte[0];

        filePath.toFile().createNewFile();

        try (var output = Files.newOutputStream(filePath)) {
            output.write(arrayExpected);
            output.flush();
        }

        var byteArray = client.executeGet(filePath.toString());
        assertArrayEquals(arrayExpected, byteArray);
    }

    @Test
    void testGetNonExisting(@TempDir Path tmpDir) throws IOException {
        assertThrows(FileNotFoundException.class,
                () -> client.executeGet(tmpDir.resolve("test").toAbsolutePath().toString()));
    }

    @Test
    void testListMultipleClients(@TempDir Path tmpDir) throws IOException {
        tmpDir.resolve("aaa").toFile().createNewFile();
        tmpDir.resolve("bbb").toFile().mkdirs();
        tmpDir.resolve("bbb").resolve("ccc").toFile().createNewFile();

        var otherClient = new FTPClient();
        otherClient.connect("localhost", 9999);

        var list = client.executeList(tmpDir.toString());
        list.sort(Comparator.comparing(a -> a.left));

        var otherList = otherClient.executeList(tmpDir.toString());
        otherList.sort(Comparator.comparing(a -> a.left));

        var listExpected = Arrays.asList(
                new ImmutablePair<>("aaa", false),
                new ImmutablePair<>("bbb", true));

        assertEquals(listExpected, list);
        assertEquals(listExpected, otherList);
    }

}