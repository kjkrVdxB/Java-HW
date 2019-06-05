package com.example.p2cw4;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Integer.min;

/**
 * A simple FTP server
 * The protocol is following:
 * <p>
 * All basic types are encoded as per DataOutputStream specification.
 * Each request should start with the length of the request body, one 4 byte integer. Requests of length more than
 * {@code MAX_REQUEST_LENGTH} result in the server disconnect since they can be abusing.
 * The client can ask two types of queries, GET and LIST. Request body starts with a 4 byte integer indicating
 * the request type, 1 for GET and 2 for LIST. After it is the string indicating the path, as encoded by
 * {@code DataOutputStream.writeUTF(String)}. // TODO string restrictions
 * <p>
 * The answer body for the GET request starts with a 4 byte integer indicating the length of the resulting file.
 * The length is -1 if the file was not found, there is nothing after the length in this case.
 * <p>
 * The answer body for the LIST request starts with a 4 byte integer indicating the number of entries in the requested
 * directory, or -1 if it was not found. Then that amount of records of type {@code (String, Boolean)}, where the string is
 * the entry name, and the boolean is true iff the entry is a directory.
 */
public class FTPServer {
    public final static int REQUEST_LIST = 1;
    public final static int REQUEST_GET = 2;
    public final static int ANSWER_FILE_NOT_FOUND = -1;
    public final static int MAX_REQUEST_LENGTH = 2048;
    private final static int MAX_BYTES_READ = 4096;
    private final static int MAX_BYTES_WRITE = 4096;
    private static final long EXECUTOR_AWAIT_TERMINATION_SECONDS = 5;

    private Thread worker;
    private ServerSocketChannel serverSocketChannel;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private volatile Selector selector;

    /**
     * Create new FTPServer listening on {@code port}
     *
     * @throws IOException if something goes wrong
     */
    public FTPServer(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
    }

    /**
     * Start the server
     *
     * @throws IOException in case the start fails
     */
    public void start() throws IOException {
        selector = Selector.open();
        worker = new Thread(() -> {
            try {
                try {
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                    return;
                }
                for (; ; ) {
                    try {
                        selector.select();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    if (!selector.isOpen()) {
                        return;
                    }
                    synchronized (selector.selectedKeys()) {
                        var keys = selector.selectedKeys().iterator();
                        while (keys.hasNext()) {
                            var key = keys.next();
                            if (!key.isValid()) {
                                keys.remove();
                                continue;
                            }
                            if (key.isAcceptable()) {
                                SocketChannel socketChannel = null;
                                try {
                                    socketChannel = ((ServerSocketChannel) key.channel()).accept();
                                    socketChannel.configureBlocking(false);
                                    socketChannel.register(selector, SelectionKey.OP_READ, new ChannelHandler(socketChannel));
                                    System.out.println(socketChannel.getRemoteAddress().toString() + " connected");
                                } catch (IOException e) {
                                    try {
                                        if (socketChannel != null) {
                                            socketChannel.close();
                                        }
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                    e.printStackTrace();
                                    continue;
                                }
                                keys.remove();
                            } else if (key.isReadable()) {
                                var channelHandler = (ChannelHandler) key.attachment();
                                try {
                                    if (!channelHandler.processRead()) {
                                        System.out.println(channelHandler.socketChannel.getRemoteAddress().toString() + " disconnected");
                                        key.channel().close();
                                        continue;
                                    }
                                } catch (IOException e) {
                                    tryCloseAfterChannelException(e, key.channel());
                                    continue;
                                }
                                keys.remove();
                            } else if (key.isWritable()) {
                                var channelHandler = (ChannelHandler) key.attachment();
                                try {
                                    channelHandler.processWrite();
                                } catch (IOException e) {
                                    tryCloseAfterChannelException(e, key.channel());
                                    continue;
                                }
                                if (!channelHandler.shouldWrite) {
                                    try {
                                        channelHandler.socketChannel.register(selector, SelectionKey.OP_READ, channelHandler);
                                    } catch (ClosedChannelException e) {
                                        e.printStackTrace();
                                        continue;
                                    }
                                }
                                keys.remove();
                            }
                        }
                    }
                }
            } catch (ClosedSelectorException e) {
                // time to go out
            }
        });
        worker.start();
    }

    private static void tryCloseAfterChannelException(@NonNull IOException e, @NonNull SelectableChannel channel) {
        try {
            channel.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        e.printStackTrace();
    }

    /**
     * Stop the server. Waits for all the threads to stop.
     *
     * @throws InterruptedException if interrupted while locked
     */
    public void stop() throws InterruptedException {
        try {
            selector.close();
        } catch (IOException e) {
            System.out.println("Exception while closing the selector");
            e.printStackTrace();
        }
        worker.interrupt();
        worker.join();
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            System.out.println("Exception while closing the server socket channel");
            e.printStackTrace();
        }
        executor.shutdownNow();
        executor.awaitTermination(EXECUTOR_AWAIT_TERMINATION_SECONDS, TimeUnit.SECONDS);
    }

    /** Class for reading and writing to the socket channel, and also starting processing threads */
    private class ChannelHandler {
        @NonNull
        private final ByteBuffer lengthBuffer = ByteBuffer.allocate(4); // 4 bytes for storing 'int'
        @NonNull
        private ByteBuffer dataBuffer;
        private boolean readLength = false;
        @NonNull
        private final SocketChannel socketChannel;
        @NonNull
        private final RequestHandler handler;
        private int requestLength = 0;
        private boolean shouldWrite = false;
        @NonNull
        private ByteBuffer answerBuffer;

        private ChannelHandler(@NonNull SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            handler = new RequestHandler(socketChannel.getRemoteAddress().toString());
        }

        /**
         * Read the request. First it's length, then the request itself. Once the request have been read
         * start a new thread for it's processing. On one call at most {@code MAX_BYTES_READ} bytes of request are read.
         * Also if the request length is more that {@code MAX_REQUEST_LENGTH} it is considered malformed and
         * the end-of-stream is reported.
         *
         * @return false if the end-of-stream was reached or the request length is too long
         * @throws IOException in case of read error
         */
        boolean processRead() throws IOException {
            if (!readLength) {
                if (socketChannel.read(lengthBuffer) == -1) {
                    return false;
                }
                lengthBuffer.flip();
                if (lengthBuffer.remaining() == 4) {
                    requestLength = lengthBuffer.getInt();
                    lengthBuffer.clear();
                    if (requestLength > MAX_REQUEST_LENGTH) {
                        // Basic protection against DOS
                        return false;
                    }
                    dataBuffer = ByteBuffer.allocate(requestLength);
                    readLength = true;
                } else {
                    lengthBuffer.compact();
                    return true;
                }
            }
            dataBuffer.limit(min(dataBuffer.position() + MAX_BYTES_READ, dataBuffer.capacity()));
            if (socketChannel.read(dataBuffer) == -1) {
                return false;
            }
            dataBuffer.flip();
            if (dataBuffer.remaining() == requestLength) {
                socketChannel.register(selector, 0);
                executor.execute(() -> {
                    byte[] answer = handler.handleRequest(dataBuffer.array());
                    dataBuffer.clear();

                    if (answer == null) {
                        try {
                            socketChannel.close();
                        } catch (IOException e) {
                            // ???
                        }
                        return;
                    }

                    answerBuffer = ByteBuffer.allocate(4 + answer.length); // Length of answer + answer itself
                    answerBuffer.putInt(answer.length);
                    answerBuffer.put(answer);
                    answerBuffer.flip();
                    readLength = false;
                    shouldWrite = true;
                    try {
                        socketChannel.register(selector, SelectionKey.OP_WRITE, this);
                        selector.wakeup();
                    } catch (ClosedChannelException e) {
                        // well we go now
                    }
                });
            } else {
                dataBuffer.compact();
            }
            return true;
        }

        /**
         * Try to write at most {@code MAX_BYTES_WRITE} of answerBuffer
         *
         * @throws IOException in case of writing error
         */
        void processWrite() throws IOException {
            answerBuffer.limit(min(answerBuffer.position() + MAX_BYTES_WRITE, answerBuffer.capacity()));
            socketChannel.write(answerBuffer);
            if (answerBuffer.position() == answerBuffer.capacity()) {
                shouldWrite = false;
            }
        }
    }

    /** Class for handling request processing */
    private static class RequestHandler {
        private final @NonNull String client;

        private RequestHandler(@NonNull String client) {
            this.client = client;
        }


        /**
         * Get an answer to the given request
         *
         * @return null in case the request
         */
        private byte @Nullable [] handleRequest(byte @NonNull [] request) {
            try (var inputStream = new ByteArrayInputStream(request);
                 var dataInputStream = new DataInputStream(inputStream);
                 var outputStream = new ByteArrayOutputStream()) {

                int type = dataInputStream.readInt();
                if (type == REQUEST_LIST) {
                    handleListRequest(inputStream, outputStream);
                } else if (type == REQUEST_GET) {
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
                    dataOutputStream.writeInt(ANSWER_FILE_NOT_FOUND);
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
}
