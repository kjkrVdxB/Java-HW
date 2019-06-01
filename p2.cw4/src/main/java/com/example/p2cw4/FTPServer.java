package com.example.p2cw4;

import org.apache.commons.io.FileUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FTPServer {
    private Thread worker;
    private ServerSocket serverSocket;

    public FTPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        worker = new Thread(() -> {
            try {
                for (; ; ) {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> {
                        handle(socket);
                    }).start();
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
                if (type == 1) {
                    System.out.println(client + " requested list of directory '" + pathString + "'");
                    if (!Files.isDirectory(path)) {
                        dataOutputStream.writeInt(-1);
                        dataOutputStream.flush();
                        continue;
                    }
                    var contents = Files.list(path).collect(Collectors.toList());
                    dataOutputStream.writeInt(contents.size());
                    dataOutputStream.flush();
                    for (var contentsPath: contents) {
                        dataOutputStream.writeUTF(contentsPath.getFileName().toString());
                        dataOutputStream.writeBoolean(contentsPath.toFile().isDirectory());
                    }
                    dataOutputStream.flush();
                } else if (type == 2) {
                    System.out.println(client + " requested file '" + pathString + "'");
                    if (!Files.isRegularFile(path)) {
                        dataOutputStream.writeInt(-1);
                        dataOutputStream.flush();
                        continue;
                    }
                    int size = (int) FileUtils.sizeOf(path.toFile());
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
