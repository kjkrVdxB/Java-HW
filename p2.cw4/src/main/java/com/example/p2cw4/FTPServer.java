package com.example.p2cw4;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FTPServer {
    public final static int REQUEST_LIST = 1;
    public final static int REQUEST_GET = 2;
    public final static int ANSWER_FILE_NOT_FOUND = -1;

    private Thread worker;
    private ServerSocket serverSocket;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public FTPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        worker = new Thread(() -> {
            try {
                for (; ; ) {
                    Socket socket = serverSocket.accept();
                    executor.execute(() -> handleSocket(socket));
                }
            } catch (IOException e) {
                // server socket closed?
            }
        });
        worker.start();
    }

    public void stop() throws InterruptedException, IOException {
        serverSocket.close();
        worker.join();
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static void handleSocket(Socket socket) {
        String client = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        System.out.println(client + " incoming");
        var handler = new RequestHandler(client);
        try {
            answerRequestsWith(socket, handler::handleRequest);
        } catch (IOException e) {
            // probably client disconnected?
        }
        System.out.println(client + " disconnected");
    }

    private static void answerRequestsWith(@NonNull Socket socket, @NonNull Function<byte[], byte[]> processor) throws IOException {
        var outputStream = socket.getOutputStream();
        var dataOutputStream = new DataOutputStream(outputStream);
        var inputStream = socket.getInputStream();
        var dataInputStream = new DataInputStream(inputStream);

        //noinspection InfiniteLoopStatement
        for (; ; ) {
            int requestLength = dataInputStream.readInt();
            byte[] request = inputStream.readNBytes(requestLength);

            byte[] answer = processor.apply(request);

            dataOutputStream.writeInt(answer.length);
            dataOutputStream.flush();
            outputStream.write(answer);
            outputStream.flush();
        }
    }

    private static class RequestHandler {
        private final @NonNull String client;

        public RequestHandler(@NonNull String client) {
            this.client = client;
        }

        private byte[] handleRequest(byte[] request) {
            try (var inputStream = new ByteArrayInputStream(request);
                 var dataInputStream = new DataInputStream(inputStream);
                 var outputStream = new ByteArrayOutputStream()) {

                int type = dataInputStream.readInt();
                if (type == REQUEST_LIST) {
                    handleListRequest(inputStream, outputStream);
                } else if (type == REQUEST_GET) {
                    handleGetRequest(inputStream, outputStream);
                }
                return outputStream.toByteArray();
            } catch (IOException e) {
                // not going to happen
                throw new AssertionError();
            }
        }

        private void handleListRequest(@NonNull InputStream in, @NonNull OutputStream out) {
            try (var dataInputStream = new DataInputStream(in);
                 var dataOutputStream = new DataOutputStream(out)) {
                var pathString = dataInputStream.readUTF();
                var path = Path.of(pathString);
                System.out.println(client + " requested list of directory '" + pathString + "'");
                if (!Files.isDirectory(path)) {
                    dataOutputStream.writeInt(ANSWER_FILE_NOT_FOUND);
                    dataOutputStream.flush();
                    return;
                }
                List<Path> contents;
                try (var directoryStream = Files.list(path)) {
                    contents = directoryStream.collect(Collectors.toList());
                }
                dataOutputStream.writeInt(contents.size());
                dataOutputStream.flush();
                for (var contentsPath: contents) {
                    dataOutputStream.writeUTF(contentsPath.getFileName().toString());
                    dataOutputStream.writeBoolean(Files.isDirectory(contentsPath));
                }
                dataOutputStream.flush();
            } catch (IOException e) {
                // not going to happen
                throw new AssertionError();
            }
        }

        private void handleGetRequest(@NonNull InputStream in, @NonNull OutputStream out) {
            try (var dataInputStream = new DataInputStream(in);
                 var dataOutputStream = new DataOutputStream(out)) {
                var pathString = dataInputStream.readUTF();
                var path = Path.of(pathString);
                System.out.println(client + " requested file '" + pathString + "'");
                if (!Files.isRegularFile(path)) {
                    dataOutputStream.writeInt(ANSWER_FILE_NOT_FOUND);
                    dataOutputStream.flush();
                    return;
                }
                int size = (int) Files.size(path);
                dataOutputStream.writeInt(size);
                dataOutputStream.flush();
                Files.copy(path, out);
                dataOutputStream.flush();
            } catch (IOException e) {
                // not going to happen
                throw new AssertionError();
            }
        }
    }
}
