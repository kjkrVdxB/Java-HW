package com.example.ftpgui;

import org.junit.jupiter.api.RepeatedTest;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import static com.example.ftpgui.PacketUtil.constructPacket;
import static com.example.ftpgui.server.FTPServer.REQUEST_GET;
import static com.example.ftpgui.server.FTPServer.REQUEST_LIST;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class FTPClientTest {
    private static final int TEST_RUNS = 10;
    private static final int TEST_PORT = 10001;
    private static final int CONNECTION_TIMEOUT_MILLIS = 2000;

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

        var simpleServer = new SimpleServer(expectedRequest.length, answer, TEST_PORT);
        otherClient.connect("localhost", TEST_PORT, CONNECTION_TIMEOUT_MILLIS);
        assertThrows(FileNotFoundException.class, () -> otherClient.executeGet("abacaba"));
        byte[] clientRequest = simpleServer.getClientRequest();

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

        var simpleServer = new SimpleServer(expectedRequest.length, answer, TEST_PORT);
        otherClient.connect("localhost", TEST_PORT, CONNECTION_TIMEOUT_MILLIS);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, otherClient.executeGet("abacaba"));
        byte[] clientRequest = simpleServer.getClientRequest();

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

        var simpleServer = new SimpleServer(expectedRequest.length, answer, TEST_PORT);
        otherClient.connect("localhost", TEST_PORT, CONNECTION_TIMEOUT_MILLIS);
        assertThrows(FileNotFoundException.class, () -> otherClient.executeList("abacaba"));
        byte[] clientRequest = simpleServer.getClientRequest();

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

        var simpleServer = new SimpleServer(expectedRequest.length, answer, TEST_PORT);
        otherClient.connect("localhost", TEST_PORT, CONNECTION_TIMEOUT_MILLIS);
        assertEquals(List.of(new FTPClient.ListingItem(FTPClient.ListingItem.Type.DIRECTORY, "a"),
                             new FTPClient.ListingItem(FTPClient.ListingItem.Type.FILE, "b")), otherClient.executeList("abacaba"));
        byte[] clientRequest = simpleServer.getClientRequest();

        assertArrayEquals(expectedRequest, clientRequest);

        otherClient.disconnect();
    }

    /** A simple server that receives a packet of the given length and gives it out later */
    private static class SimpleServer {
        private ServerSocket serverSocket;
        private Thread serverThread;
        private ByteArrayOutputStream clientRequest = new ByteArrayOutputStream();
        private SimpleServer(int numberBytesToReceive, byte[] bytesToWrite, int port) throws IOException {
            serverSocket = new ServerSocket(port);
            serverThread = new Thread(() -> {
                try {
                    var socket = serverSocket.accept();
                    var socketInput = socket.getInputStream();
                    for (int i = 0; i < numberBytesToReceive; ++i) {
                        var byteRead = socketInput.read();
                        clientRequest.write(byteRead);
                    }
                    var socketOutput = socket.getOutputStream();
                    socketOutput.write(bytesToWrite);
                    socketOutput.flush();
                    serverSocket.close();
                } catch (IOException ignore) {
                }
            });
            serverThread.start();
        }

        private byte[] getClientRequest() throws InterruptedException {
            serverThread.join();
            return clientRequest.toByteArray();
        }
    }
}
