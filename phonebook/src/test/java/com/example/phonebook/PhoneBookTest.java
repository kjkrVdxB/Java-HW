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

import static org.junit.jupiter.api.Assertions.*;

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
    public void testAddEntry() throws PhoneBookStorageException {
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
    public void testDeleteEntry() throws PhoneBookStorageException {
        phonebook.deleteEntry("ddd", "aaa");
        phonebook.deleteEntry("aaa", "000");
        assertEquals(List.of(new Entry("aaa", "111"),
                             new Entry("bbb", "000")), phonebook.getEntries());
    }

    @Test
    public void testUpdateName() throws PhoneBookStorageException {
        phonebook.updateName("aaa", "111", "bbb");
        phonebook.updateName("ddd", "aaa", "kkk");
        phonebook.updateName("aba", "000", "cab");
        assertEquals(List.of(new Entry("aaa", "000"),
                             new Entry("bbb", "000"),
                             new Entry("bbb", "111"),
                             new Entry("kkk", "aaa")), phonebook.getEntries());
    }

    @Test
    public void testUpdateNameMerge() throws PhoneBookStorageException {
        phonebook.updateName("aaa", "000", "bbb");
        assertEquals(List.of(new Entry("aaa", "111"),
                             new Entry("bbb", "000"),
                             new Entry("ddd", "aaa")), phonebook.getEntries());
    }

    @Test
    public void testUpdateNumber() throws PhoneBookStorageException {
        phonebook.updateNumber("aaa", "111", "222");
        phonebook.updateNumber("ddd", "aaa", "222");
        phonebook.updateNumber("aba", "000", "010");
        assertEquals(List.of(new Entry("aaa", "000"),
                             new Entry("aaa", "222"),
                             new Entry("bbb", "000"),
                             new Entry("ddd", "222")), phonebook.getEntries());
    }

    @Test
    public void testUpdateNumberMerge() throws PhoneBookStorageException {
        phonebook.updateNumber("aaa", "000", "111");
        assertEquals(List.of(new Entry("aaa", "111"),
                             new Entry("bbb", "000"),
                             new Entry("ddd", "aaa")), phonebook.getEntries());
    }

    @Test
    public void testDeleteAllEntries() throws PhoneBookStorageException {
        phonebook.deleteAllEntries();
        assertEquals(List.of(), phonebook.getEntries());
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
}