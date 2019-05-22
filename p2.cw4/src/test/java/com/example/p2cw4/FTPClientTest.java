package com.example.p2cw4;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FTPTest {
    FTPServer server = new FTPServer(9999);


    FTPTest() throws IOException {}

    @BeforeAll
    void startServer() {
        server.start();
    }

    @AfterAll
    void stopServer() throws InterruptedException {
        server.stop();
    }

    @Test
    void testList(@TempDir Path tmpDir) throws IOException {
        tmpDir.resolve("aaa").toFile().createNewFile();
        tmpDir.resolve("bbb").toFile().mkdirs();
        tmpDir.resolve("bbb").resolve("ccc").toFile().createNewFile();

    }

}