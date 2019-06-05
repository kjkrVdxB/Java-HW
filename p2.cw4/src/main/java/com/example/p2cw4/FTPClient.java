package com.example.p2cw4;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.p2cw4.FTPServer.*;

/** A simple FTP client. See the details of the protocol in {@link FTPServer} */
public class FTPClient {
    @Nullable
    private Socket socket;

    /**
     * Try to connect to the given {@code address} and {@code port}. Disconnects first if already connected.
     *
     * @throws IOException if the connection fails
     */
    public void connect(@NonNull String address, int port) throws IOException {
        if (isConnected()) {
            disconnect();
        }
        socket = new Socket(address, port);
    }

    /**
     * Disconnect from the server. Does nothing if not connected.
     *
     * @throws IOException if the disconnect failed
     */
    public void disconnect() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    /**
     * Check if we successfully connected
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * Execute the LIST query. Returns the entries in the {@code path} on the server.
     *
     * @throws IOException if there was a connection error
     * @throws FileNotFoundException if there is no such directory on the server
     * @throws IllegalStateException if not connected
     */
    @NonNull
    public List<ListingItem> executeList(@NonNull String path) throws IOException {

        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }
        var arrayStream = new ByteArrayOutputStream();

        encodeListRequest(path, arrayStream);

        var result = decodeListAnswer(handleRequest(arrayStream.toByteArray()));
        if (result == null) {
            throw new FileNotFoundException("File '" + path + "' not found");
        }
        return result;
    }

    private static void encodeListRequest(@NonNull String path, @NonNull OutputStream stream) throws IOException {
        try (var dataStream = new DataOutputStream(stream)) {
            dataStream.writeInt(REQUEST_LIST);
            dataStream.writeUTF(path);
        }
    }

    @Nullable
    private static List<ListingItem> decodeListAnswer(byte @NonNull [] answer) throws IOException {
        try (var arrayStream = new ByteArrayInputStream(answer);
             var dataStream = new DataInputStream(arrayStream)) {
            var result = new ArrayList<ListingItem>();
            int size = dataStream.readInt();
            if (size == ANSWER_FILE_NOT_FOUND) {
                return null;
            }
            for (int i = 0; i < size; ++i) {
                String name = dataStream.readUTF();
                boolean isDirectory = dataStream.readBoolean();
                result.add(new ListingItem(isDirectory ? ListingItem.Type.DIRECTORY : ListingItem.Type.FILE, name));
            }
            return result;
        }
    }

    /**
     * Execute the GET query. Returns the contents of the file on the server.
     *
     * @throws IOException if there was a connection error
     * @throws FileNotFoundException if there is no such directory on the server
     * @throws IllegalStateException if not connected
     */
    public byte @NonNull [] executeGet(@NonNull String path) throws IOException {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }
        var arrayStream = new ByteArrayOutputStream();

        encodeGetRequest(path, arrayStream);

        var result = decodeGetAnswer(handleRequest(arrayStream.toByteArray()));
        if (result == null) {
            throw new FileNotFoundException("File '" + path + "' not found");
        }
        return result;
    }

    /** Handle (send) one request */
    private byte @NonNull [] handleRequest(byte @NonNull [] request) throws IOException {
        assert socket != null;
        var outputStream = socket.getOutputStream();
        var dataOutputStream = new DataOutputStream(outputStream);
        var inputStream = socket.getInputStream();
        var dataInputStream = new DataInputStream(inputStream);

        dataOutputStream.writeInt(request.length);
        dataOutputStream.flush();
        outputStream.write(request);
        outputStream.flush();

        int answerLength = dataInputStream.readInt();
        return inputStream.readNBytes(answerLength);
    }

    private static void encodeGetRequest(@NonNull String path, @NonNull OutputStream stream) throws IOException {
        try (var dataStream = new DataOutputStream(stream)) {
            dataStream.writeInt(REQUEST_GET);
            dataStream.writeUTF(path);
        }
    }

    private static byte @Nullable [] decodeGetAnswer(byte @NonNull [] answer) throws IOException {
        byte[] result;
        try (var arrayStream = new ByteArrayInputStream(answer);
             var dataStream = new DataInputStream(arrayStream)) {
            int size = dataStream.readInt();
            if (size == ANSWER_FILE_NOT_FOUND) {
                return null;
            }
            result = arrayStream.readNBytes(size); // should actually read everything
        }
        return result;
    }

    /** Describes a list entry */
    public static class ListingItem {
        @NonNull
        final Type type;
        @NonNull
        final String name;

        public ListingItem(@NonNull Type type, @NonNull String name) {
            this.type = type;
            this.name = name;
        }

        public Type getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public enum Type {
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