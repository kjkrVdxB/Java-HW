package com.example.p2cw4;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.p2cw4.FTPServer.*;

public class FTPClient {
    private Socket socket;

    public void connect(@NonNull String address, int port) throws IOException {
        socket = new Socket(address, port);
    }

    public void disconnect() throws IOException {
        socket.close();
        socket = null;
    }

    public boolean isConnected() {
        return socket != null;
    }

    @NonNull
    public List<ListingItem> executeList(@NonNull String path) throws IOException {
        var arrayStream = new ByteArrayOutputStream();

        encodeListRequest(path, arrayStream);

        var result = decodeListAnswer(handleRequest(arrayStream.toByteArray()));
        if (result == null) {
            throw new FileNotFoundException("File '" + path + "' not found");
        }
        return result;
    }

    private static void encodeListRequest(@NonNull String path, @NonNull OutputStream stream) {
        try (var dataStream = new DataOutputStream(stream)) {
            dataStream.writeInt(REQUEST_LIST);
            dataStream.writeUTF(path);
        } catch (IOException e) {
            // not going to happen
            throw new AssertionError();
        }
    }

    @Nullable
    private static List<ListingItem> decodeListAnswer(byte @NonNull [] answer) {
        try (var arrayStream = new ByteArrayInputStream(answer);
             var dataStream = new DataInputStream(arrayStream)) {
            var result = new ArrayList<ListingItem>();
            int size = dataStream.readInt();
            if (size == ANSWER_FILE_NOT_FOUND) {
                return null;
            }
            for (int i = 0; i < size; ++i) {
                String name = dataStream.readUTF();
                boolean isDir = dataStream.readBoolean();
                result.add(new ListingItem(isDir ? ListingItem.Type.DIRECTORY : ListingItem.Type.FILE, name));
            }
            return result;
        } catch (IOException e) {
            // not going to happen
            throw new AssertionError();
        }
    }

    public byte @NonNull [] executeGet(@NonNull String path) throws IOException {
        var arrayStream = new ByteArrayOutputStream();

        encodeGetRequest(path, arrayStream);

        var result = decodeGetAnswer(handleRequest(arrayStream.toByteArray()));
        if (result == null) {
            throw new FileNotFoundException("File '" + path + "' not found");
        }
        return result;
    }

    private byte @NonNull [] handleRequest(byte @NonNull [] request) throws IOException {
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

    private static void encodeGetRequest(@NonNull String path, @NonNull OutputStream stream) {
        try (var dataStream = new DataOutputStream(stream)) {
            dataStream.writeInt(REQUEST_GET);
            dataStream.writeUTF(path);
        } catch (IOException e) {
            // not going to happen
            throw new AssertionError();
        }
    }

    private static byte @Nullable [] decodeGetAnswer(byte @NonNull [] answer) {
        byte[] result;
        try (var arrayStream = new ByteArrayInputStream(answer);
             var dataStream = new DataInputStream(arrayStream)) {
            int size = dataStream.readInt();
            if (size == ANSWER_FILE_NOT_FOUND) {
                return null;
            }
            result = arrayStream.readNBytes(size); // should actually read everything
        } catch (IOException e) {
            // not going to happen
            throw new AssertionError();
        }
        return result;
    }

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