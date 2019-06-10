package com.example.ftpgui;

import com.example.ftpgui.server.FTPServer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.example.ftpgui.PacketUtil.constructPacket;
import static com.example.ftpgui.server.FTPServer.REQUEST_GET;
import static com.example.ftpgui.server.FTPServer.REQUEST_LIST;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class FTPServerTest {
    private static final int TEST_RUNS = 10;
    private static final int TEST_PORT = 10001;

    @SuppressWarnings("WeakerAccess")
    public @TempDir
    Path tmpDir;

    @RepeatedTest(TEST_RUNS)
    void testServerGetNotFoundDirect() throws IOException, InterruptedException {
        var anotherServer = new FTPServer(TEST_PORT);
        anotherServer.start();

        var expectedAnswer = constructPacket(data -> {
            try {
                data.writeInt(-1);
            } catch (IOException ignored) {
            }
        });

        assertArrayEquals(expectedAnswer, simpleRequest(TEST_PORT, constructPacket(data -> {
            try {
                data.writeInt(REQUEST_GET);
                data.writeUTF("abacaba");
            } catch (IOException ignored) {
            }
        }), expectedAnswer.length));

        anotherServer.stop();
    }

    @RepeatedTest(TEST_RUNS)
    void testServerGetFoundDirect() throws IOException, InterruptedException {
        Files.write(tmpDir.resolve("abacaba"), new byte[]{1, 2, 3, 4});

        var anotherServer = new FTPServer(TEST_PORT);
        anotherServer.start();

        var expectedAnswer = constructPacket(data -> {
            try {
                data.writeInt(4);
                data.write(new byte[]{1, 2, 3, 4});
            } catch (IOException ignored) {
            }
        });

        assertArrayEquals(expectedAnswer, simpleRequest(TEST_PORT, constructPacket(data -> {
            try {
                data.writeInt(REQUEST_GET);
                data.writeUTF(tmpDir.resolve("abacaba").toString());
            } catch (IOException ignored) {
            }
        }), expectedAnswer.length));

        anotherServer.stop();
    }

    @RepeatedTest(TEST_RUNS)
    void testServerListNotFoundDirect() throws IOException, InterruptedException {
        var anotherServer = new FTPServer(TEST_PORT);
        anotherServer.start();

        var expectedAnswer = constructPacket(data -> {
            try {
                data.writeInt(-1);
            } catch (IOException ignored) {
            }
        });

        assertArrayEquals(expectedAnswer, simpleRequest(TEST_PORT, constructPacket(data -> {
            try {
                data.writeInt(REQUEST_LIST);
                data.writeUTF("abacaba");
            } catch (IOException ignored) {
            }
        }), expectedAnswer.length));

        anotherServer.stop();
    }

    @RepeatedTest(TEST_RUNS)
    void testServerListFoundDirect() throws IOException, InterruptedException {
        Files.createDirectories(tmpDir.resolve("abacaba").resolve("a"));
        Files.createFile(tmpDir.resolve("abacaba").resolve("b"));

        var anotherServer = new FTPServer(TEST_PORT);
        anotherServer.start();

        var expectedAnswer = constructPacket(data -> {
            try {
                data.writeInt(2);
                data.writeUTF("a");
                data.writeBoolean(true);
                data.writeUTF("b");
                data.writeBoolean(false);
            } catch (IOException ignored) {
            }
        });

        assertArrayEquals(expectedAnswer, simpleRequest(TEST_PORT, constructPacket(data -> {
            try {
                data.writeInt(REQUEST_LIST);
                data.writeUTF(tmpDir.resolve("abacaba").toString());
            } catch (IOException ignored) {
            }
        }), expectedAnswer.length));

        anotherServer.stop();
    }

    private static byte[] simpleRequest(int port, byte[] request, int numberBytesToRead) throws IOException {
        var socket = new Socket("localhost", port);
        socket.getOutputStream().write(request);
        socket.getOutputStream().flush();
        var result = socket.getInputStream().readNBytes(numberBytesToRead);
        socket.close();
        return result;
    }
}
