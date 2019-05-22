package com.example.p2cw4;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ClientCli {
    public static void main(String[] args) throws IOException {
        System.out.println("Simple FTP client");
        System.out.println("Type 'help' for help");
        boolean exit = false;
        var ftpClient = new FTPClient();
        while (!exit) {
            var input = prompt();
            var commands = input.split(" ");
            if (commands.length == 0) {
                continue;
            }
            switch (commands[0]) {
                case "exit":
                    exit = true;
                    break;
                case "help":
                    printHelp();
                    break;
                case "connect":
                    ftpClient.connect(commands[1], Integer.valueOf(commands[2]));
                    break;
                case "disconnect":
                    ftpClient.disconnect();
                    break;
                case "list":
                    printList(ftpClient.executeList(commands[1]));
                    break;
                case "get":
                    var contents = ftpClient.executeGet(commands[1]);
                    var file = new File(commands[2]);
                    FileUtils.writeByteArrayToFile(file, contents);
                    break;
            }
        }
    }

    private static void printList(List<ImmutablePair<String, Boolean>> list) {
        System.out.println("Files in the directory");
        for (var entry: list) {
            System.out.println((entry.getRight() ? "d ": "f ") + entry.getLeft());
        }
    }

    private static @NonNull String prompt() {
        System.out.print("ftp> ");
        System.out.flush();
        Scanner s = new Scanner(System.in);
        return s.nextLine();
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help - print this help");
        System.out.println("  connect 'address' 'port' - connect to supplied address and port");
        System.out.println("  disconnect - close connection");
        System.out.println("  list 'path' - query server for a list of files in path");
        System.out.println("  get 'path' 'to' -  download file at 'path' to file 'to' in current directory");
        System.out.println("  exit - exit the program");
    }
}
