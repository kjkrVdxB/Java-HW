package com.example.ftpgui.server;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Class for handling request processing */
class RequestHandler {
    private final @NonNull String client;

    RequestHandler(@NonNull String client) {
        this.client = client;
    }

    /**
     * Get an answer to the given request
     *
     * @return null in case the request
     */
    byte @Nullable [] handleRequest(byte @NonNull [] request) {
        try (var inputStream = new ByteArrayInputStream(request);
             var dataInputStream = new DataInputStream(inputStream);
             var outputStream = new ByteArrayOutputStream()) {

            int type = dataInputStream.readInt();
            if (type == FTPServer.REQUEST_LIST) {
                handleListRequest(inputStream, outputStream);
            } else if (type == FTPServer.REQUEST_GET) {
                handleGetRequest(inputStream, outputStream);
            } else {
                return null;
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            // probably array overrun
            return null;
        }
    }

    /**
     * Reads the LIST request parameters from {@code in} and writes the answer to {@code out}
     *
     * @throws IOException in case of stream errors
     */
    private void handleListRequest(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        try (var dataInputStream = new DataInputStream(in);
             var dataOutputStream = new DataOutputStream(out)) {
            var pathString = dataInputStream.readUTF();
            var path = Path.of(pathString);
            System.out.println(client + " requested list of directory '" + pathString + "'");
            if (!Files.isDirectory(path)) {
                dataOutputStream.writeInt(FTPServer.ANSWER_FILE_NOT_FOUND);
                dataOutputStream.flush();
                return;
            }
            List<Path> contents;
            try (var directoryStream = Files.list(path)) {
                contents = directoryStream.collect(Collectors.toList());
            }
            contents.sort(Comparator.comparing(filePath -> filePath.getFileName().toString()));
            dataOutputStream.writeInt(contents.size());
            dataOutputStream.flush();
            for (var contentsPath: contents) {
                dataOutputStream.writeUTF(contentsPath.getFileName().toString());
                dataOutputStream.writeBoolean(Files.isDirectory(contentsPath));
            }
            dataOutputStream.flush();
        }
    }

    /**
     * Reads the GET request parameters from {@code in} and writes the answer to {@code out}
     *
     * @throws IOException in case of stream errors
     */
    private void handleGetRequest(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        try (var dataInputStream = new DataInputStream(in);
             var dataOutputStream = new DataOutputStream(out)) {
            var pathString = dataInputStream.readUTF();
            var path = Path.of(pathString);
            System.out.println(client + " requested file '" + pathString + "'");
            if (!Files.isRegularFile(path)) {
                dataOutputStream.writeInt(FTPServer.ANSWER_FILE_NOT_FOUND);
                dataOutputStream.flush();
                return;
            }
            int size = (int) Files.size(path);
            dataOutputStream.writeInt(size);
            dataOutputStream.flush();
            Files.copy(path, out);
            dataOutputStream.flush();
        }
    }
}
