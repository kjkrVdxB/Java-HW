package com.example.phonebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class PhoneBookCliClient {
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String HELP_STRING = "Enter one of the following numbers:" + NEWLINE +
                                              "0 - exit" + NEWLINE +
                                              "1 - add record" + NEWLINE +
                                              "2 - list numbers by name" + NEWLINE +
                                              "3 - list names by number" + NEWLINE +
                                              "4 - delete an entry by name and number" + NEWLINE +
                                              "5 - update name in the entry specified by current name and number" + NEWLINE +
                                              "6 - update number in the entry specified by current name and number" + NEWLINE +
                                              "7 - list all entries" + NEWLINE +
                                              "8 - delete all entries" + NEWLINE +
                                              "and then fill in the requested fields" + NEWLINE;

    public static void main(String[] args) throws IOException {
        String dbPath = args.length == 0 ? ":memory:" : args[0];
        runCli(System.in, System.out, dbPath);
    }

    static void runCli(InputStream in, PrintStream out, String dbPath) throws IOException {
        assert in != null;
        assert out != null;
        assert dbPath != null;
        var inputReader = new BufferedReader(new InputStreamReader(in));
        try (var phoneBook = new PhoneBook(dbPath)) {
            out.println("Phone book CLI client. Try 'help' for a list of supported commands.");

            // ends when user inputs 0 as an option
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
                switch (option) {
                    case 0:
                        return;
                    case 1: {
                        out.print("Name: ");
                        var name = inputReader.readLine();
                        out.print("Number: ");
                        var number = inputReader.readLine();
                        phoneBook.addEntry(name, number);
                        break;
                    }
                    case 2: {
                        out.print("Name: ");
                        var name = inputReader.readLine();
                        for (var number: phoneBook.getNumbersByName(name)) {
                            out.println(number);
                        }
                        break;
                    }
                    case 3: {
                        out.print("Number: ");
                        var number = inputReader.readLine();
                        for (var name: phoneBook.getNamesByNumber(number)) {
                            out.println(name);
                        }
                        break;
                    }
                    case 4: {
                        out.print("Name: ");
                        var name = inputReader.readLine();
                        out.print("Number: ");
                        var number = inputReader.readLine();
                        phoneBook.deleteEntry(name, number);
                        break;
                    }
                    case 5: {
                        out.print("Current name: ");
                        var name = inputReader.readLine();
                        out.print("Current number: ");
                        var number = inputReader.readLine();
                        out.print("New name: ");
                        var newName = inputReader.readLine();
                        phoneBook.updateName(name, number, newName);
                        break;
                    }
                    case 6: {
                        out.print("Current name: ");
                        var name = inputReader.readLine();
                        out.print("Current number: ");
                        var number = inputReader.readLine();
                        out.print("New number: ");
                        var newNumber = inputReader.readLine();
                        phoneBook.updateNumber(name, number, newNumber);
                        break;
                    }
                    case 7:
                        for (var entry: phoneBook.getEntries()) {
                            out.println(entry.getName() + ": " + entry.getNumber());
                        }
                        break;
                    default:  // option == 8
                        phoneBook.deleteAllEntries();
                        break;
                }
            }
        }
    }

    private static void writeWrongCommandMessage(PrintStream out, String command) {
        out.println("Number from range [0;8] or 'help' expected, got '" + command + "'. Try again.");
    }
}
