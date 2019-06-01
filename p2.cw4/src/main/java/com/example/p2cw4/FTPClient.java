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
        var outputStream = socket.getOutputStream();
        var dataOutputStream = new DataOutputStream(outputStream);
        var inputStream = socket.getInputStream();
        var dataInputStream = new DataInputStream(inputStream);
        dataOutputStream.writeInt(1);
        dataOutputStream.writeUTF(path);
        dataOutputStream.flush();
        int size = dataInputStream.readInt();
        if (size == -1) {
            throw new FileNotFoundException("File '" + path + "' not found");
        }
        var result = new ArrayList<ImmutablePair<String, Boolean>>();
        for (int i = 0; i < size; ++i) {
            String name = dataInputStream.readUTF();
            boolean isDir = dataInputStream.readBoolean();
            result.add(new ImmutablePair<>(name, isDir));
        }
        return result;
    }

    public byte[] executeGet(@NonNull String path) throws IOException {
        var outputStream = socket.getOutputStream();
        var dataOutputStream = new DataOutputStream(outputStream);
        var inputStream = socket.getInputStream();
        var dataInputStream = new DataInputStream(inputStream);
        dataOutputStream.writeInt(2);
        dataOutputStream.writeUTF(path);
        dataOutputStream.flush();
        int size = dataInputStream.readInt();
        if (size == -1) {
            throw new FileNotFoundException("File '" + path + "' not found");
        }
        return dataInputStream.readNBytes(size);
    }
}