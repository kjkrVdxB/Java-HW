package com.example.p2cw4;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FTPServer {
    public final static int REQUEST_LIST = 1;
    public final static int REQUEST_GET = 2;

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
                    executor.execute(() -> handle(socket));
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

    private static void handle(Socket socket) {
        String client = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        System.out.println(client + " incoming");
        try {
            var inputStream = socket.getInputStream();
            var dataInputStream = new DataInputStream(inputStream);
            var outputStream = socket.getOutputStream();
            var dataOutputStream = new DataOutputStream(outputStream);
            //noinspection InfiniteLoopStatement
            for (; ; ) {
                int type = dataInputStream.readInt();
                var pathString = dataInputStream.readUTF();
                var path = Paths.get(pathString);
                if (type == REQUEST_LIST) {
                    System.out.println(client + " requested list of directory '" + pathString + "'");
                    if (!Files.isDirectory(path)) {
                        dataOutputStream.writeInt(-1);
                        dataOutputStream.flush();
                        continue;
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
                } else if (type == REQUEST_GET) {
                    System.out.println(client + " requested file '" + pathString + "'");
                    if (!Files.isRegularFile(path)) {
                        dataOutputStream.writeInt(-1);
                        dataOutputStream.flush();
                        continue;
                    }
                    int size = (int) Files.size(path);
                    dataOutputStream.writeInt(size);
                    dataOutputStream.flush();
                    Files.copy(path, dataOutputStream);
                    dataOutputStream.flush();
                }
            }
        } catch (IOException e) {
            // probably client disconnected?
        }
        System.out.println(client + " disconnected");
    }
}
