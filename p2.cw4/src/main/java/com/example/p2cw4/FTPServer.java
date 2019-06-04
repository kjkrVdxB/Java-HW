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

public class FTPServer {
    final static int REQUEST_LIST = 1;
    final static int REQUEST_GET = 2;
    final static int ANSWER_FILE_NOT_FOUND = -1;
    private final static int MAX_BYTES_READ = 4096;
    private final static int MAX_BYTES_WRITE = 4096;
    private final static int MAX_REQUEST_LENGTH = 2048;

    private Thread worker;
    private ServerSocketChannel serverSocketChannel;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private volatile Selector selector;

    public FTPServer(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
    }

    public void start() throws IOException {
        selector = Selector.open();
        worker = new Thread(() -> {
            try {
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                for (; ; ) {
                    selector.select();
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
                                var socketChannel = ((ServerSocketChannel) key.channel()).accept();
                                socketChannel.configureBlocking(false);
                                socketChannel.register(selector, SelectionKey.OP_READ, new ChannelHandler(socketChannel));
                                keys.remove();
                            } else if (key.isReadable()) {
                                var channelHandler = (ChannelHandler) key.attachment();
                                if (!channelHandler.processRead()) {
                                    key.channel().close();
                                    continue;
                                }
                                keys.remove();
                            } else if (key.isWritable()) {
                                var channelHandler = (ChannelHandler) key.attachment();
                                if (!channelHandler.processWrite()) {
                                    key.channel().close();
                                    continue;
                                }
                                if (!channelHandler.shouldWrite) {
                                    channelHandler.socketChannel.register(selector, SelectionKey.OP_READ, channelHandler);
                                }
                                keys.remove();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                // TODO
            } catch (ClosedSelectorException e) {
                // time to go out
            }
        });
        worker.start();
    }

    public void stop() throws InterruptedException, IOException {
        selector.close();
        worker.interrupt();
        worker.join();
        serverSocketChannel.close();
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    private class ChannelHandler {
        @NonNull
        private final ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
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

                    answerBuffer = ByteBuffer.allocate(answer.length + 4);
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

        boolean processWrite() throws IOException {
            answerBuffer.limit(min(answerBuffer.position() + MAX_BYTES_WRITE, answerBuffer.capacity()));
            if (socketChannel.write(answerBuffer) == -1) {
                return false;
            }
            if (answerBuffer.position() == answerBuffer.capacity()) {
                shouldWrite = false;
            }
            return true;
        }
    }

    private static class RequestHandler {
        private final @NonNull String client;

        private RequestHandler(@NonNull String client) {
            this.client = client;
        }

        private byte @Nullable [] handleRequest(byte @NonNull [] request) {
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
                // probably array overrun
                return null;
            }
        }

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
