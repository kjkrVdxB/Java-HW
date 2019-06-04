package com.example.p2cw4;

import com.example.p2cw4.FTPClient.ListingItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class FTPTest {
    private static final int TEST_RUNS = 10;
    private static final int TEST_PORT = 10000;
    private FTPServer server = new FTPServer(TEST_PORT);
    private FTPClient client = new FTPClient();
    @SuppressWarnings("WeakerAccess")
    public @TempDir
    Path tmpDir;

    FTPTest() throws IOException {}

    @BeforeEach
    void init() throws IOException {
        server.start();
        client.connect("localhost", TEST_PORT);
    }

    @AfterEach
    void finish() throws InterruptedException, IOException {
        client.disconnect();
        server.stop();
    }

    @RepeatedTest(TEST_RUNS)
    void testListBasic() throws IOException {
        Files.createFile(tmpDir.resolve("aaa"));
        Files.createDirectories(tmpDir.resolve("bbb"));
        Files.createFile(tmpDir.resolve("bbb").resolve("ccc"));

        var list = client.executeList(tmpDir.toString());
        list.sort(Comparator.comparing(ListingItem::getName));

        var listExpected = Arrays.asList(
                new ListingItem(ListingItem.Type.FILE, "aaa"),
                new ListingItem(ListingItem.Type.DIRECTORY, "bbb"));

        assertEquals(listExpected, list);
    }

    @RepeatedTest(TEST_RUNS)
    void testEmptyList() throws IOException {
        var list = client.executeList(tmpDir.toString());
        list.sort(Comparator.comparing(ListingItem::getName));
        assertEquals(0, list.size());
    }

    @RepeatedTest(TEST_RUNS)
    void testListSubdirectory() throws IOException {
        // aaa -> bbb -> ccc -----  ddd(dir)
        //                    \
        //                    `---  eee(file)

        var path = tmpDir.resolve("aaa").resolve("bbb").resolve("ccc");

        Files.createDirectories(path.resolve("ddd"));
        Files.createFile(path.resolve("eee"));

        var list = client.executeList(path.toString());
        list.sort(Comparator.comparing(ListingItem::getName));

        var listExpected = Arrays.asList(
                new ListingItem(ListingItem.Type.DIRECTORY, "ddd"),
                new ListingItem(ListingItem.Type.FILE, "eee"));

        assertEquals(listExpected, list);
    }

    @RepeatedTest(TEST_RUNS)
    void testGetBasic() throws IOException {
        var filePath = tmpDir.resolve("test");
        var arrayExpected = new byte[]{1, 2, 3, 4, 5};

        Files.createFile(filePath);

        Files.write(filePath, arrayExpected);

        var byteArray = client.executeGet(filePath.toString());
        assertArrayEquals(arrayExpected, byteArray);
    }

    @RepeatedTest(TEST_RUNS)
    void testGetEmpty() throws IOException {
        var filePath = tmpDir.resolve("test");
        var arrayExpected = new byte[0];

        Files.createFile(filePath);

        var byteArray = client.executeGet(filePath.toString());
        assertArrayEquals(arrayExpected, byteArray);
    }

    @RepeatedTest(TEST_RUNS)
    void testGetNonExisting() {
        assertThrows(FileNotFoundException.class,
                     () -> client.executeGet(tmpDir.resolve("test").toString()));
    }

    @RepeatedTest(TEST_RUNS)
    void testListNonExisting() {
        assertThrows(FileNotFoundException.class,
                     () -> client.executeList(tmpDir.resolve("test").toString()));
    }

    @RepeatedTest(TEST_RUNS)
    void testIsConnected() throws IOException {
        assertTrue(client.isConnected());

        var otherClient = new FTPClient();

        assertFalse(otherClient.isConnected());

        otherClient.connect("localhost", TEST_PORT);
        assertTrue(otherClient.isConnected());

        otherClient.disconnect();
        assertFalse(otherClient.isConnected());
    }

    @RepeatedTest(TEST_RUNS)
    void testListMultipleClients() throws IOException {
        Files.createFile(tmpDir.resolve("aaa"));
        Files.createDirectories(tmpDir.resolve("bbb"));
        Files.createDirectories(tmpDir.resolve("bbb").resolve("ccc"));

        var otherClient = new FTPClient();
        otherClient.connect("localhost", TEST_PORT);

        var list = client.executeList(tmpDir.toString());
        list.sort(Comparator.comparing(ListingItem::getName));

        var otherList = otherClient.executeList(tmpDir.toString());
        otherList.sort(Comparator.comparing(ListingItem::getName));

        var listExpected = Arrays.asList(
                new ListingItem(ListingItem.Type.FILE, "aaa"),
                new ListingItem(ListingItem.Type.DIRECTORY, "bbb"));

        assertEquals(listExpected, list);
        assertEquals(listExpected, otherList);

        otherClient.disconnect();
    }

    @RepeatedTest(TEST_RUNS)
    void testMultipleQueries() throws IOException {
        Files.createFile(tmpDir.resolve("aaa"));
        Files.createDirectories(tmpDir.resolve("bbb"));
        Files.createDirectories(tmpDir.resolve("bbb").resolve("ccc"));

        var list = client.executeList(tmpDir.toString());
        list.sort(Comparator.comparing(ListingItem::getName));

        var listExpected = Arrays.asList(
                new ListingItem(ListingItem.Type.FILE, "aaa"),
                new ListingItem(ListingItem.Type.DIRECTORY, "bbb"));

        assertEquals(listExpected, list);

        list = client.executeList(tmpDir.resolve("bbb").toString());
        list.sort(Comparator.comparing(ListingItem::getName));

        assertEquals(Collections.singletonList(new ListingItem(ListingItem.Type.DIRECTORY, "ccc")), list);
    }

    @RepeatedTest(5)
    void testMultipleConcurrentQueries() throws IOException {
        final int QUERIES_COUNT = 10;
        Files.createFile(tmpDir.resolve("aaa"));
        Files.createFile(tmpDir.resolve("bbb"));
        Files.createFile(tmpDir.resolve("ccc"));

        var otherClient = new FTPClient();
        otherClient.connect("localhost", TEST_PORT);
        var yetAnotherClient = new FTPClient();
        yetAnotherClient.connect("localhost", TEST_PORT);

        var thread1 = new Thread(() -> {
            try {
                for (int i = 0; i < QUERIES_COUNT; ++i) {
                    client.executeList(tmpDir.toString());
                }
            } catch (IOException ignored) {
            }
        });

        var thread2 = new Thread(() -> {
            try {
                for (int i = 0; i < QUERIES_COUNT; ++i) {
                    otherClient.executeList(tmpDir.toString());
                }
            } catch (IOException ignored) {
            }
        });

        var thread3 = new Thread(() -> {
            try {
                for (int i = 0; i < QUERIES_COUNT; ++i) {
                    yetAnotherClient.executeGet(tmpDir.resolve("aaa").toString());
                }
            } catch (IOException ignored) {
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException ignored) {
        }

        otherClient.disconnect();
        yetAnotherClient.disconnect();
    }
}