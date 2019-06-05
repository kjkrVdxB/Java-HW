package com.example.ftpgui;

import java.io.IOException;

public class TestClient {
    public static void main(String[] args) throws IOException {
        var client = new FTPClient();
        client.connect("localhost", 9999);
        System.out.println(client.executeList("/etc"));
    }
}
