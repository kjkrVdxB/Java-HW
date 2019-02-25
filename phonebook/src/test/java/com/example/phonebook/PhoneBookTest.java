package com.example.phonebook;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
    public void init() throws IOException, PhoneBookStorageException, SQLException {
        tmpFile = File.createTempFile("phone_book_test", ".db");
        phonebook = new PhoneBook(tmpFile.getAbsolutePath());
        connection = DriverManager.getConnection("jdbc:sqlite:" + tmpFile.getAbsolutePath());

        phonebook.addEntry("ddd", "aaa");
        phonebook.addEntry("aaa", "111");
        phonebook.addEntry("bbb", "000");
        phonebook.addEntry("aaa", "000");
    }

    @AfterEach
    public void finish() throws PhoneBookStorageException, SQLException {
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
    public void testAddEntryDuplicates() throws PhoneBookStorageException, SQLException {
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
    public void testGetEntries() throws PhoneBookStorageException {
        assertEquals(List.of(new Entry("aaa", "000"),
                             new Entry("aaa", "111"),
                             new Entry("bbb", "000"),
                             new Entry("ddd", "aaa")), phonebook.getEntries());
    }

    @Test
    public void testGetNumbersByName() throws PhoneBookStorageException {
        assertEquals(List.of("000", "111"), phonebook.getNumbersByName("aaa"));
        assertEquals(List.of("000"), phonebook.getNumbersByName("bbb"));
        assertEquals(List.of("aaa"), phonebook.getNumbersByName("ddd"));
        assertEquals(List.of(), phonebook.getNumbersByName("ccc"));
    }

    @Test
    public void testGetNamesByNumber() throws PhoneBookStorageException {
        assertEquals(List.of("aaa", "bbb"), phonebook.getNamesByNumber("000"));
        assertEquals(List.of("aaa"), phonebook.getNamesByNumber("111"));
        assertEquals(List.of("ddd"), phonebook.getNamesByNumber("aaa"));
        assertEquals(List.of(), phonebook.getNamesByNumber("222"));
    }

    @Test
    public void testDeleteEntry() throws PhoneBookStorageException, SQLException {
        phonebook.deleteEntry("ddd", "aaa");
        phonebook.deleteEntry("aaa", "000");
        checkTables(List.of("aaa", "bbb"),
                    List.of(new Entry("aaa", "111"),
                            new Entry("bbb", "000")),
                    List.of("000", "111"));
    }

    @Test
    public void testUpdateName() throws PhoneBookStorageException, SQLException {
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
    public void testUpdateNameMerge() throws PhoneBookStorageException, SQLException {
        phonebook.updateName("aaa", "000", "bbb");
        phonebook.updateName("aaa", "111", "bbb");
        checkTables(List.of("bbb", "ddd"),
                    List.of(new Entry("bbb", "000"),
                            new Entry("bbb", "111"),
                            new Entry("ddd", "aaa")),
                    List.of("000", "111", "aaa"));
    }

    @Test
    public void testUpdateNumber() throws PhoneBookStorageException, SQLException {
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
    public void testUpdateNumberMerge() throws PhoneBookStorageException, SQLException {
        phonebook.updateNumber("aaa", "000", "111");
        phonebook.updateNumber("bbb", "000", "111");
        checkTables(List.of("aaa", "bbb", "ddd"),
                    List.of(new Entry("aaa", "111"),
                            new Entry("bbb", "111"),
                            new Entry("ddd", "aaa")),
                    List.of("111", "aaa"));
    }

    @Test
    public void testDeleteAllEntries() throws PhoneBookStorageException, SQLException {
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

    private void checkTables(List<String> names, List<Entry> entries, List<String> numbers) throws SQLException {
        var resultNames = connection.createStatement().executeQuery("select Name from Person order by Name");
        var namesIterator = names.iterator();
        while (resultNames.next()) {
            assertTrue(namesIterator.hasNext());
            assertEquals(namesIterator.next(), resultNames.getString(1));
        }
        assertFalse(namesIterator.hasNext());

        var resultEntries = connection.createStatement().executeQuery("select Name, Number from Entry " +
                                                                      "inner join Person on Person.Id = PersonId " +
                                                                      "inner join Phone on Phone.Id = PhoneId " +
                                                                      "order by Name, Number");
        var entriesIterator = entries.iterator();
        while (resultEntries.next()) {
            assertTrue(entriesIterator.hasNext());
            assertEquals(entriesIterator.next(),
                         new Entry(resultEntries.getString(1), resultEntries.getString(2)));
        }
        assertFalse(entriesIterator.hasNext());

        var resultNumbers = connection.createStatement().executeQuery("select Number from Phone order by Number");
        var numbersIterator = numbers.iterator();
        while (resultNumbers.next()) {
            assertTrue(numbersIterator.hasNext());
            assertEquals(numbersIterator.next(), resultNumbers.getString(1));
        }
        assertFalse(numbersIterator.hasNext());
    }
}