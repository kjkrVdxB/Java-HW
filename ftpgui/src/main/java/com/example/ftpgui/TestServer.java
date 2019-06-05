package com.example.ftpgui;

import java.io.IOException;

public class TestServer {
    public static void main(String[] args) throws IOException {
        var server = new FTPServer(9999);
        server.start();
    }
}
