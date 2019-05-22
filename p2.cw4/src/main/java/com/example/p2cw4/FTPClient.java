package com.example.p2cw4;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FTPClient {
    private Socket socket;

    public void connect(@NonNull String address, int port) throws IOException {
        socket = new Socket(address, port);
    }

    public void disconnect() throws IOException {
        socket.close();
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public List<ImmutablePair<String, Boolean>> executeList(@NonNull String path) throws IOException {
        try (var outputStream = socket.getOutputStream();
             var dataOutputStream = new DataOutputStream(outputStream);
             var inputStream = socket.getInputStream();
             var dataInputStream = new DataInputStream(inputStream)) {
            dataOutputStream.writeInt(1);
            dataOutputStream.writeUTF(path);
            int size = dataInputStream.readInt();
            if (size == -1) {
                throw new FileNotFoundException("File " + path + "not found");
            }
            System.out.println(size);
            var result = new ArrayList<ImmutablePair<String, Boolean>>();
            for (int i = 0; i < size; ++i) {
                result.add(new ImmutablePair<>(dataInputStream.readUTF(), dataInputStream.readBoolean()));
            }
            return result;
        }
    }

    public byte[] executeGet(@NonNull String path) throws IOException {
        try (var outputStream = socket.getOutputStream();
             var dataOutputStream = new DataOutputStream(outputStream);
             var inputStream = socket.getInputStream();
             var dataInputStream = new DataInputStream(inputStream)) {
            dataOutputStream.writeInt(1);
            dataOutputStream.writeUTF(path);
            int size = dataInputStream.readInt();
            if (size == -1) {
                throw new FileNotFoundException("Path " + path + "not found");
            }
            return dataInputStream.readNBytes(size);
        }
    }
}
