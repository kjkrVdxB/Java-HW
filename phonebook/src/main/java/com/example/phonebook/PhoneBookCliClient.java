package com.example.phonebook;

import java.io.*;

public class PhoneBookCliClient {
    private static final String NEWLINE = System.lineSeparator();
    private static final String HELP_STRING = "Enter one of the following numbers:" + NEWLINE +
                                              "0 - exit" + NEWLINE +
                                              "1 - add an entry" + NEWLINE +
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
                        var name = prompt(inputReader, out, "Name");
                        var number = prompt(inputReader, out, "Number");
                        if (phoneBook.containsEntry(name, number)) {
                            out.println("The phone book already contains an equivalent entry");
                        } else {
                            phoneBook.addEntry(name, number);
                        }
                        break;
                    }
                    case 2: {
                        var name = prompt(inputReader, out, "Name");
                        for (var number: phoneBook.getNumbersByName(name)) {
                            out.println(number);
                        }
                        break;
                    }
                    case 3: {
                        var number = prompt(inputReader, out, "Number");
                        for (var name: phoneBook.getNamesByNumber(number)) {
                            out.println(name);
                        }
                        break;
                    }
                    case 4: {
                        var name = prompt(inputReader, out, "Name");
                        var number = prompt(inputReader, out, "Number");
                        phoneBook.deleteEntry(name, number);
                        break;
                    }
                    case 5: {
                        var name = prompt(inputReader, out, "Current name");
                        var number = prompt(inputReader, out, "Current number");
                        var newName = prompt(inputReader, out, "New name");
                        phoneBook.updateName(name, number, newName);
                        break;
                    }
                    case 6: {
                        var name = prompt(inputReader, out, "Current name");
                        var number = prompt(inputReader, out, "Current number");
                        var newNumber = prompt(inputReader, out, "New number");
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

    private static String prompt(BufferedReader in, PrintStream out, String message) throws IOException {
        out.print(message + ": ");
        out.flush();
        return in.readLine();
    }
}
