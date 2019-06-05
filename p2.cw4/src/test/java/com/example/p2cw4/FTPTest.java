package com.example.p2cw4;

import com.example.p2cw4.FTPClient.ListingItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static com.example.p2cw4.FTPServer.REQUEST_GET;
import static com.example.p2cw4.FTPServer.REQUEST_LIST;
import static org.junit.jupiter.api.Assertions.*;

class FTPTest {
    private static final int TEST_RUNS = 10;
    private static final int TEST_PORT = 10000;
    private static final int SECOND_TEST_PORT = 10001;
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

    @RepeatedTest(TEST_RUNS)
    void testClientGetRequestDirect() throws IOException, InterruptedException {
        var otherClient = new FTPClient();

        var expectedRequest = constructPacket(data -> {
            try {
                data.writeInt(REQUEST_GET);
                data.writeUTF("abacaba");
            } catch (IOException ignore) {
            }
        });

        var answer = constructPacket(data -> {
            try {
                data.writeInt(-1);
            } catch (IOException ignore) {
            }
        });

        var simpleServer = new SimpleServer(expectedRequest.length, answer, SECOND_TEST_PORT);
        otherClient.connect("localhost", SECOND_TEST_PORT);
        assertThrows(FileNotFoundException.class, () -> otherClient.executeGet("abacaba"));
        byte[] clientRequest = simpleServer.getResult();

        assertArrayEquals(expectedRequest, clientRequest);

        otherClient.disconnect();
    }

    @RepeatedTest(TEST_RUNS)
    void testClientGetAnswerDirect() throws IOException, InterruptedException {
        var otherClient = new FTPClient();

        var expectedRequest = constructPacket(data -> {
            try {
                data.writeInt(REQUEST_GET);
                data.writeUTF("abacaba");
            } catch (IOException ignore) {
            }
        });

        var answer = constructPacket(data -> {
            try {
                data.writeInt(4);
                data.write(new byte[]{1, 2, 3, 4});
            } catch (IOException ignore) {
            }
        });

        var simpleServer = new SimpleServer(expectedRequest.length, answer, SECOND_TEST_PORT);
        otherClient.connect("localhost", SECOND_TEST_PORT);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, otherClient.executeGet("abacaba"));
        byte[] clientRequest = simpleServer.getResult();

        assertArrayEquals(expectedRequest, clientRequest);

        otherClient.disconnect();
    }

    @RepeatedTest(TEST_RUNS)
    void testClientListRequestDirect() throws IOException, InterruptedException {
        var otherClient = new FTPClient();

        var expectedRequest = constructPacket(data -> {
            try {
                data.writeInt(REQUEST_LIST);
                data.writeUTF("abacaba");
            } catch (IOException ignore) {
            }
        });

        var answer = constructPacket(data -> {
            try {
                data.writeInt(-1);
            } catch (IOException ignore) {
            }
        });

        var simpleServer = new SimpleServer(expectedRequest.length, answer, SECOND_TEST_PORT);
        otherClient.connect("localhost", SECOND_TEST_PORT);
        assertThrows(FileNotFoundException.class, () -> otherClient.executeList("abacaba"));
        byte[] clientRequest = simpleServer.getResult();

        assertArrayEquals(expectedRequest, clientRequest);

        otherClient.disconnect();
    }

    @RepeatedTest(TEST_RUNS)
    void testClientListAnswerDirect() throws IOException, InterruptedException {
        var otherClient = new FTPClient();

        var expectedRequest = constructPacket(data -> {
            try {
                data.writeInt(REQUEST_LIST);
                data.writeUTF("abacaba");
            } catch (IOException ignore) {
            }
        });

        var answer = constructPacket(data -> {
            try {
                data.writeInt(2);
                data.writeUTF("a");
                data.writeBoolean(true);
                data.writeUTF("b");
                data.writeBoolean(false);
            } catch (IOException ignore) {
            }
        });

        var simpleServer = new SimpleServer(expectedRequest.length, answer, SECOND_TEST_PORT);
        otherClient.connect("localhost", SECOND_TEST_PORT);
        assertEquals(List.of(new ListingItem(ListingItem.Type.DIRECTORY, "a"),
                             new ListingItem(ListingItem.Type.FILE, "b")), otherClient.executeList("abacaba"));
        byte[] clientRequest = simpleServer.getResult();

        assertArrayEquals(expectedRequest, clientRequest);

        otherClient.disconnect();
    }

    private byte[] constructPacket(Consumer<DataOutputStream> streamWriter) {
        var packetBody = new ByteArrayOutputStream();
        var packetBodyData = new DataOutputStream(packetBody);

        streamWriter.accept(packetBodyData);

        var packet = new ByteArrayOutputStream();
        var packetData = new DataOutputStream(packet);

        try {
            packetData.writeInt(packetBody.size());
            packetData.write(packetBody.toByteArray());
        } catch (IOException ignored) {
        }

        return packet.toByteArray();
    }

    private static class SimpleServer {
        private ServerSocket serverSocket;
        private Thread serverThread;
        private ByteArrayOutputStream result = new ByteArrayOutputStream();
        private SimpleServer(int numberBytesToReceive, byte[] bytesToWrite, int port) throws IOException {
            serverSocket = new ServerSocket(port);
            serverThread = new Thread(() -> {
                try {
                    var socket = serverSocket.accept();
                    var socketInput = socket.getInputStream();
                    for (int i = 0; i < numberBytesToReceive; ++i) {
                        var byteRead = socketInput.read();
                        result.write(byteRead);
                    }
                    var socketOutput = socket.getOutputStream();
                    socketOutput.write(bytesToWrite);
                    serverSocket.close();
                } catch (IOException ignore) {
                }
            });
            serverThread.start();
        }

        private byte[] getResult() throws InterruptedException {
            serverThread.join();
            return result.toByteArray();
        }
    }
}