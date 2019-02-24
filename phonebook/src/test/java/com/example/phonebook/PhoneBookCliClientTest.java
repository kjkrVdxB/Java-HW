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

    @Test
    void testHelpMessage() {
        testCli("Phone book CLI client. Try 'help' for a list of supported commands.\n" +
                "Enter one of the following numbers:\n" +
                "0 - exit\n" +
                "1 - add record\n" +
                "2 - list numbers by name\n" +
                "3 - list names by number\n" +
                "4 - delete an entry by name and number\n" +
                "5 - update name in the entry specified by current name and number\n" +
                "6 - update number in the entry specified by current name and number\n" +
                "7 - list all entries\n" +
                "8 - delete all entries\n" +
                "and then fill in the requested fields\n", "help\n0\n");
    }

    @Test
    void testSmallSession() {
        var input = "1\n" +
                    "Dave\n" +
                    "0000\n" +
                    "3\n" +
                    "0000\n" +
                    "2\n" +
                    "Dave\n" +
                    "7\n" +
                    "4\n" +
                    "Dave\n" +
                    "0000\n" +
                    "7\n" +
                    "0\n";
        var expectedOutput = "Phone book CLI client. Try 'help' for a list of supported commands.\n" +
                             "Name: " +
                             "Number: " +
                             "Number: " +
                             "Dave\n" +
                             "Name: " +
                             "0000\n" +
                             "Dave: 0000\n" +
                             "Name: " +
                             "Number: ";
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
