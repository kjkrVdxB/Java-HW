package com.example.p2cw4;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.p2cw4.FTPServer.REQUEST_GET;
import static com.example.p2cw4.FTPServer.REQUEST_LIST;

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

    public List<ListingItem> executeList(@NonNull String path) throws IOException {
        var outputStream = socket.getOutputStream();
        var dataOutputStream = new DataOutputStream(outputStream);
        var inputStream = socket.getInputStream();
        var dataInputStream = new DataInputStream(inputStream);
        dataOutputStream.writeInt(REQUEST_LIST);
        dataOutputStream.writeUTF(path);
        dataOutputStream.flush();
        int size = dataInputStream.readInt();
        if (size == -1) {
            throw new FileNotFoundException("File '" + path + "' not found");
        }
        var result = new ArrayList<ListingItem>();
        for (int i = 0; i < size; ++i) {
            String name = dataInputStream.readUTF();
            boolean isDir = dataInputStream.readBoolean();
            result.add(new ListingItem(isDir ? ListingItem.ItemType.DIRECTORY : ListingItem.ItemType.FILE, name));
        }
        return result;
    }

    public byte[] executeGet(@NonNull String path) throws IOException {
        var outputStream = socket.getOutputStream();
        var dataOutputStream = new DataOutputStream(outputStream);
        var inputStream = socket.getInputStream();
        var dataInputStream = new DataInputStream(inputStream);
        dataOutputStream.writeInt(REQUEST_GET);
        dataOutputStream.writeUTF(path);
        dataOutputStream.flush();
        int size = dataInputStream.readInt();
        if (size == -1) {
            throw new FileNotFoundException("File '" + path + "' not found");
        }
        return dataInputStream.readNBytes(size);
    }

    public static class ListingItem {
        @NonNull final ItemType type;
        @NonNull final String name;

        public ListingItem(@NonNull ItemType type, @NonNull String name) {
            this.type = type;
            this.name = name;
        }

        public ItemType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public enum ItemType {
            FILE,
            DIRECTORY
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ListingItem)) {
                return false;
            }
            ListingItem other = (ListingItem) obj;
            return type == other.type && name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name);
        }

        @Override
        public String toString() {
            return type.name() + " " + name;
        }
    }
}