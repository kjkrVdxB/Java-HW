package com.example.p2cw4;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
                    if (commands.length != 1) {
                        printHelp();
                        continue;
                    }
                    exit = true;
                    break;
                case "help":
                    printHelp();
                    break;
                case "connect":
                    if (commands.length != 3) {
                        printHelp();
                        continue;
                    }
                    ftpClient.connect(commands[1], Integer.valueOf(commands[2]));
                    break;
                case "disconnect":
                    if (commands.length != 1) {
                        printHelp();
                        continue;
                    }
                    ftpClient.disconnect();
                    break;
                case "list":
                    if (commands.length != 2) {
                        printHelp();
                        continue;
                    }
                    printList(ftpClient.executeList(commands[1]));
                    break;
                case "get":
                    if (commands.length != 3) {
                        printHelp();
                        continue;
                    }
                    try {
                        var contents = ftpClient.executeGet(commands[1]);
                        var path = Path.of(commands[2]);
                        Files.write(path, contents);
                    } catch (FileNotFoundException exception) {
                        System.out.println("No such file");
                    }
                    break;
                default:
                    System.out.println("Unknown command: " + commands[0]);
                    printHelp();
                    break;
            }
        }
    }

    private static void printList(List<ImmutablePair<String, Boolean>> list) {
        System.out.println("Files in the directory");
        for (var entry: list) {
            System.out.println((entry.getRight() ? "d " : "f ") + entry.getLeft());
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
