package com.example.phonebook;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.phonebook.PhoneBook.Entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneBookTest {
    private PhoneBook phonebook;
    private Connection connection;
    private File tmpFile;

    @BeforeEach
    public void init() throws IOException, SQLException {
        tmpFile = File.createTempFile("phone_book_test", ".db");
        phonebook = new PhoneBook(tmpFile.getAbsolutePath());
        connection = DriverManager.getConnection("jdbc:sqlite:" + tmpFile.getAbsolutePath());

        phonebook.addEntry("ddd", "aaa");
        phonebook.addEntry("aaa", "111");
        phonebook.addEntry("bbb", "000");
        phonebook.addEntry("aaa", "000");
    }

    @AfterEach
    public void finish() throws SQLException {
        connection.close();
        phonebook.close();
        assertTrue(tmpFile.delete());
    }

    @Test
    public void testAddEntry() throws SQLException {
        checkTables(List.of("aaa", "bbb", "ddd"),
                    List.of(new Entry("aaa", "000"),
                            new Entry("aaa", "111"),
                            new Entry("bbb", "000"),
                            new Entry("ddd", "aaa")),
                    List.of("000", "111", "aaa"));
    }

    @Test
    public void testAddEntryDuplicates() throws SQLException {
        phonebook.addEntry("ddd", "aaa");
        phonebook.addEntry("aaa", "000");
        checkTables(List.of("aaa", "bbb", "ddd"),
                    List.of(new Entry("aaa", "000"),
                            new Entry("aaa", "111"),
                            new Entry("bbb", "000"),
                            new Entry("ddd", "aaa")),
                    List.of("000", "111", "aaa"));
    }

    @Test
    public void testGetEntries() {
        assertEquals(List.of(new Entry("aaa", "000"),
                             new Entry("aaa", "111"),
                             new Entry("bbb", "000"),
                             new Entry("ddd", "aaa")), phonebook.getEntries());
    }

    @Test
    public void testGetNumbersByName() {
        assertEquals(List.of("000", "111"), phonebook.getNumbersByName("aaa"));
        assertEquals(List.of("000"), phonebook.getNumbersByName("bbb"));
        assertEquals(List.of("aaa"), phonebook.getNumbersByName("ddd"));
        assertEquals(List.of(), phonebook.getNumbersByName("ccc"));
    }

    @Test
    public void testGetNamesByNumber() {
        assertEquals(List.of("aaa", "bbb"), phonebook.getNamesByNumber("000"));
        assertEquals(List.of("aaa"), phonebook.getNamesByNumber("111"));
        assertEquals(List.of("ddd"), phonebook.getNamesByNumber("aaa"));
        assertEquals(List.of(), phonebook.getNamesByNumber("222"));
    }

    @Test
    public void testDeleteEntry() throws SQLException {
        phonebook.deleteEntry("aaa", "000");
        checkTables(List.of("aaa", "bbb", "ddd"),
                    List.of(new Entry("aaa", "111"),
                            new Entry("bbb", "000"),
                            new Entry("ddd", "aaa")),
                    List.of("000", "111", "aaa"));
    }

    @Test
    public void testDeleteEntryUnreferenced() throws SQLException {
        phonebook.deleteEntry("ddd", "aaa");
        checkTables(List.of("aaa", "bbb"),
                    List.of(new Entry("aaa", "000"),
                            new Entry("aaa", "111"),
                            new Entry("bbb", "000")),
                    List.of("000", "111"));
    }

    @Test
    public void testUpdateName() throws SQLException {
        phonebook.updateName("aaa", "111", "bbb");
        phonebook.updateName("ddd", "aaa", "kkk");
        phonebook.updateName("aba", "000", "cab");
        checkTables(List.of("aaa", "bbb", "kkk"),
                    List.of(new Entry("aaa", "000"),
                            new Entry("bbb", "000"),
                            new Entry("bbb", "111"),
                            new Entry("kkk", "aaa")),
                    List.of("000", "111", "aaa"));
    }

    @Test
    public void testUpdateNameMerge() throws SQLException {
        phonebook.updateName("aaa", "000", "bbb");
        checkTables(List.of("aaa", "bbb", "ddd"),
                    List.of(new Entry("aaa", "111"),
                            new Entry("bbb", "000"),
                            new Entry("ddd", "aaa")),
                    List.of("000", "111", "aaa"));
    }

    @Test
    public void testUpdateNameUnreferencedName() throws SQLException {
        phonebook.updateName("ddd", "aaa", "aaa");
        checkTables(List.of("aaa", "bbb"),
                    List.of(new Entry("aaa", "000"),
                            new Entry("aaa", "111"),
                            new Entry("aaa", "aaa"),
                            new Entry("bbb", "000")),
                    List.of("000", "111", "aaa"));
    }

    @Test
    public void testUpdateNumber() throws SQLException {
        phonebook.updateNumber("aaa", "111", "222");
        phonebook.updateNumber("ddd", "aaa", "222");
        phonebook.updateNumber("aba", "000", "010");
        checkTables(List.of("aaa", "bbb", "ddd"),
                    List.of(new Entry("aaa", "000"),
                            new Entry("aaa", "222"),
                            new Entry("bbb", "000"),
                            new Entry("ddd", "222")),
                    List.of("000", "222"));
    }

    @Test
    public void testUpdateNumberMerge() throws SQLException {
        phonebook.updateNumber("aaa", "000", "111");
        checkTables(List.of("aaa", "bbb", "ddd"),
                    List.of(new Entry("aaa", "111"),
                            new Entry("bbb", "000"),
                            new Entry("ddd", "aaa")),
                    List.of("000", "111", "aaa"));
    }

    @Test
    public void testUpdateNumberUnreferencedNumber() throws SQLException {
        phonebook.updateNumber("ddd", "aaa", "000");
        checkTables(List.of("aaa", "bbb", "ddd"),
                    List.of(new Entry("aaa", "000"),
                            new Entry("aaa", "111"),
                            new Entry("bbb", "000"),
                            new Entry("ddd", "000")),
                    List.of("000", "111"));
    }

    @Test
    public void testDeleteAllEntries() throws SQLException {
        phonebook.deleteAllEntries();
        checkTables(List.of(), List.of(), List.of());
    }

    @Test
    public void testPhoneBookStorageException() throws SQLException {
        connection.createStatement().execute("drop table Entry");
        assertThrows(PhoneBookStorageException.class, () -> phonebook.addEntry("aaa", "bbb"));
        assertThrows(PhoneBookStorageException.class, () -> phonebook.getNamesByNumber("000"));
        assertThrows(PhoneBookStorageException.class, () -> phonebook.getNumbersByName("aaa"));
        assertThrows(PhoneBookStorageException.class, () -> phonebook.deleteEntry("aaa", "000"));
        assertThrows(PhoneBookStorageException.class, () -> phonebook.updateNumber("aaa", "000", "222"));
        assertThrows(PhoneBookStorageException.class, () -> phonebook.updateName("aaa", "000", "ccc"));
        assertThrows(PhoneBookStorageException.class, () -> phonebook.getEntries());
        assertThrows(PhoneBookStorageException.class, () -> phonebook.deleteAllEntries());
    }

    private void checkTables(List<String> expectedNames,
                             List<Entry> expectedEntries,
                             List<String> expectedNumbers) throws SQLException {
        var resultNames = connection.createStatement().executeQuery("select Name from Person order by Name");
        var names = new ArrayList<String>();
        while (resultNames.next()) {
            names.add(resultNames.getString(1));
        }
        assertEquals(expectedNames, names);

        var resultEntries = connection.createStatement().executeQuery("select Name, Number from Entry " +
                                                                      "inner join Person on Person.Id = PersonId " +
                                                                      "inner join Phone on Phone.Id = PhoneId " +
                                                                      "order by Name, Number");
        var entries = new ArrayList<Entry>();
        while (resultEntries.next()) {
            entries.add(new Entry(resultEntries.getString(1), resultEntries.getString(2)));
        }
        assertEquals(expectedEntries, entries);

        var resultNumbers = connection.createStatement().executeQuery("select Number from Phone order by Number");
        var numbers = new ArrayList<String>();
        while (resultNumbers.next()) {
            numbers.add(resultNumbers.getString(1));
        }
        assertEquals(expectedNumbers, numbers);
    }
}