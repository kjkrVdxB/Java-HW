package com.example.p2cw4;

import java.io.IOException;

public class TestClient {
    public static void main(String[] args) throws IOException {
        var client = new FTPClient();
        client.connect("localhost", 9999);
        System.out.println(client.executeList("/etc/srain"));
    }
}
