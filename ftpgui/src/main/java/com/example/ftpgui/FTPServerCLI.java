package com.example.ftpgui;

import com.example.ftpgui.server.FTPServer;

import java.io.IOException;

import static java.lang.System.exit;

public class FTPServerCLI {
    public static void main(String[] args) {
        if (args.length != 1) {
            printUsageAndExit();
        }
        int port = -1;
        try {
            port = Integer.valueOf(args[0]);
        } catch (NumberFormatException e) {
            printUsageAndExit();
        }
        try {
            var server = new FTPServer(port);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printUsageAndExit() {
        System.out.println("One string argument expected: the port to listen on");
        exit(0);
    }
}
