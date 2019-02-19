package com.example.phonebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.SQLException;

public class PhoneBookCliClient {
    private static final String HELP_STRING = "Enter one of the following numbers:\n" +
                                              "0 - exit\n" +
                                              "1 - add record\n" +
                                              "2 - list numbers by name\n" +
                                              "3 - list names by number\n" +
                                              "4 - delete an entry by name and number\n" +
                                              "5 - update name in the entry specified by current name and number\n" +
                                              "6 - update number in the entry specified by current name and number\n" +
                                              "7 - list all entries\n" +
                                              "8 - delete all entries\n" +
                                              "and then fill in the requested fields\n";

    public static void main(String[] args) throws SQLException, IOException {
        String dbPath = args.length == 0 ? ":memory:" : args[0];
        runCli(System.in, System.out, dbPath);
    }

    public static void runCli(InputStream in, PrintStream out, String dbPath) throws SQLException, IOException {
        var inputReader = new BufferedReader(new InputStreamReader(in));
        try (var phoneBook = new PhoneBook(dbPath)) {
            out.println("Phone book CLI client. Try 'help' for a list of supported commands.");
            while (true) {
                var line = inputReader.readLine();
                if (line.equals("help")) {
                    out.print(HELP_STRING);
                    continue;
                }
                int option;
                try {
                    option = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    writeWrongCommandMessage(out, line);
                    continue;
                }
                if (option < 0 || option > 8) {
                    writeWrongCommandMessage(out, line);
                    continue;
                }
                if (option == 0) {
                    break;
                } else if (option == 1) {
                    out.print("Name: ");
                    var name = inputReader.readLine();
                    out.print("Number: ");
                    var number = inputReader.readLine();
                    phoneBook.addEntry(name, number);
                } else if (option == 2) {
                    out.print("Name: ");
                    var name = inputReader.readLine();
                    for (var number: phoneBook.getNumbersByName(name)) {
                        out.println(number);
                    }
                } else if (option == 3) {
                    out.print("Number: ");
                    var number = inputReader.readLine();
                    for (var name: phoneBook.getNamesByNumber(number)) {
                        out.println(name);
                    }
                } else if (option == 4) {
                    out.print("Name: ");
                    var name = inputReader.readLine();
                    out.print("Number: ");
                    var number = inputReader.readLine();
                    phoneBook.deleteEntry(name, number);
                } else if (option == 5) {
                    out.print("Current name: ");
                    var name = inputReader.readLine();
                    out.print("Current number: ");
                    var number = inputReader.readLine();
                    out.print("New name: ");
                    var newName = inputReader.readLine();
                    phoneBook.updateName(name, number, newName);
                } else if (option == 6) {
                    out.print("Current name: ");
                    var name = inputReader.readLine();
                    out.print("Current number: ");
                    var number = inputReader.readLine();
                    out.print("New number: ");
                    var newNumber = inputReader.readLine();
                    phoneBook.updateNumber(name, number, newNumber);
                } else if (option == 7 ) {
                    for (var entry: phoneBook.getEntries()) {
                        out.println(entry.getName() + ": " + entry.getNumber());
                    }
                } else { // option == 8
                    phoneBook.deleteAllEntries();
                }
            }
        }
    }

    private static void writeWrongCommandMessage(PrintStream out, String command) {
        out.println("Number from range [0;8] or 'help' expected, got '" + command + "'. Try again.");
    }
}
