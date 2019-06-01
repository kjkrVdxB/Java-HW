package com.example.p2cw4;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class FTPTest {
    private FTPServer server = new FTPServer(9999);
    private FTPClient client = new FTPClient();
    @SuppressWarnings("WeakerAccess")
    public @TempDir Path tmpDir;

    FTPTest() throws IOException {}

    @BeforeEach
    void init() throws IOException {
        server.start();
        client.connect("localhost", 9999);
    }

    @AfterEach
    void finish() throws InterruptedException, IOException {
        client.disconnect();
        server.stop();
    }

    @Test
    void testListBasic() throws IOException {
        Files.createFile(tmpDir.resolve("aaa"));
        Files.createDirectories(tmpDir.resolve("bbb"));
        Files.createFile(tmpDir.resolve("bbb").resolve("ccc"));

        var list = client.executeList(tmpDir.toString());
        list.sort(Comparator.comparing(a -> a.left));

        var listExpected = Arrays.asList(
                new ImmutablePair<>("aaa", false),
                new ImmutablePair<>("bbb", true));

        assertEquals(listExpected, list);
    }

    @Test
    void testEmptyList() throws IOException {
        var list = client.executeList(tmpDir.toString());
        list.sort(Comparator.comparing(a -> a.left));
        assertEquals(0, list.size());
    }

    @Test
    void testListSubdirectory() throws IOException {
        // aaa -> bbb -> ccc -----  ddd(dir)
        //                    \
        //                    `---  eee(file)

        var path = tmpDir.resolve("aaa").resolve("bbb").resolve("ccc");

        Files.createDirectories(path.resolve("ddd"));
        Files.createFile(path.resolve("eee"));

        var list = client.executeList(path.toString());
        list.sort(Comparator.comparing(a -> a.left));

        var listExpected = Arrays.asList(
                new ImmutablePair<>("ddd", true),
                new ImmutablePair<>("eee", false));

        assertEquals(listExpected, list);
    }

    @Test
    void testGetBasic() throws IOException {
        var filePath = tmpDir.resolve("test");
        var arrayExpected = new byte[]{1, 2, 3, 4, 5};

        Files.createFile(filePath);

        FileUtils.writeByteArrayToFile(filePath.toFile(), arrayExpected);

        var byteArray = client.executeGet(filePath.toString());
        assertArrayEquals(arrayExpected, byteArray);
    }

    @Test
    void testGetEmpty() throws IOException {
        var filePath = tmpDir.resolve("test");
        var arrayExpected = new byte[0];

        Files.createFile(filePath);

        var byteArray = client.executeGet(filePath.toString());
        assertArrayEquals(arrayExpected, byteArray);
    }

    @Test
    void testGetNonExisting() {
        assertThrows(FileNotFoundException.class,
                () -> client.executeGet(tmpDir.resolve("test").toAbsolutePath().toString()));
    }

    @Test
    void testListMultipleClients() throws IOException {
        Files.createFile(tmpDir.resolve("aaa"));
        Files.createDirectories(tmpDir.resolve("bbb"));
        Files.createDirectories(tmpDir.resolve("bbb").resolve("ccc"));

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

        otherClient.disconnect();
    }
}