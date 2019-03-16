package com.example.phonebook;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static com.example.phonebook.PhoneBookCliClient.runCli;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PhoneBookCliClientTest {
    private static final String NEWLINE = System.getProperty("line.separator");

    @Test
    void testMessages() {
        testCli("Phone book CLI client. Try 'help' for a list of supported commands." + NEWLINE +
                "Enter one of the following numbers:" + NEWLINE +
                "0 - exit" + NEWLINE +
                "1 - add record" + NEWLINE +
                "2 - list numbers by name" + NEWLINE +
                "3 - list names by number" + NEWLINE +
                "4 - delete an entry by name and number" + NEWLINE +
                "5 - update name in the entry specified by current name and number" + NEWLINE +
                "6 - update number in the entry specified by current name and number" + NEWLINE +
                "7 - list all entries" + NEWLINE +
                "8 - delete all entries" + NEWLINE +
                "and then fill in the requested fields" + NEWLINE +
                "Number from range [0;8] or 'help' expected, got 'aaa'. Try again." + NEWLINE +
                "Number from range [0;8] or 'help' expected, got '9'. Try again." + NEWLINE,
                "help" + NEWLINE +
                "aaa" + NEWLINE +
                "9" + NEWLINE +
                "0" + NEWLINE);
    }

    @Test
    void testSmallSession() {
        var input = "1" + NEWLINE +
                    "Dave" + NEWLINE +
                    "0000" + NEWLINE +
                    "3" + NEWLINE +
                    "0000" + NEWLINE +
                    "2" + NEWLINE +
                    "Dave" + NEWLINE +
                    "7" + NEWLINE +
                    "4" + NEWLINE +
                    "Dave" + NEWLINE +
                    "0000" + NEWLINE +
                    "7" + NEWLINE +
                    "1" + NEWLINE +
                    "John" + NEWLINE +
                    "0001" + NEWLINE +
                    "5" + NEWLINE +
                    "John" + NEWLINE +
                    "0001" + NEWLINE +
                    "Dave" + NEWLINE +
                    "7" + NEWLINE +
                    "6" + NEWLINE +
                    "Dave" + NEWLINE +
                    "0001" + NEWLINE +
                    "0002" + NEWLINE +
                    "7" + NEWLINE +
                    "8" + NEWLINE +
                    "7" + NEWLINE +
                    "0" + NEWLINE;
        var expectedOutput = "Phone book CLI client. Try 'help' for a list of supported commands." + NEWLINE +
                             "Name: " +
                             "Number: " +
                             "Number: " +
                             "Dave" + NEWLINE +
                             "Name: " +
                             "0000" + NEWLINE +
                             "Dave: 0000" + NEWLINE +
                             "Name: " +
                             "Number: " +
                             "Name: " +
                             "Number: " +
                             "Current name: " +
                             "Current number: " +
                             "New name: " +
                             "Dave: 0001" + NEWLINE +
                             "Current name: " +
                             "Current number: " +
                             "New number: " +
                             "Dave: 0002" + NEWLINE;
        testCli(expectedOutput, input);
    }

    private void testCli(String expectedOutput, String input) {
        var outputStream = new ByteArrayOutputStream();
        var outputPrintStream = new PrintStream(outputStream);

        assertDoesNotThrow(() -> runCli(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                                        outputPrintStream,
                                        ":memory:"));

        outputPrintStream.flush();

        assertEquals(expectedOutput, outputStream.toString());
    }
}
