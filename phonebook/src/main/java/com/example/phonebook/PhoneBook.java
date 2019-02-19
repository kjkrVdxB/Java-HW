package com.example.phonebook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhoneBook implements AutoCloseable {
    private final Connection dbConnection;

    /**
     * Create new PhoneBook with an open connection to database file named {@code dbPath}. Path can be anything that
     * SQLite-jdbc library accepts in connection string after 'jdbc:sqlite:', for example ':memory:'.
     */
    public PhoneBook(String dbPath) throws SQLException {
        dbConnection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        dbConnection.setAutoCommit(false);
        try (var create = dbConnection.createStatement()) {
            create.executeUpdate("create table if not exists Person " +
                                 "(Id integer primary key asc, Name text unique not null)");
            create.executeUpdate("create table if not exists Entry " +
                                 "(PersonId integer not null, PhoneId integer not null)");
            create.executeUpdate("create table if not exists Phone " +
                                 "(Id integer primary key asc, Number text unique not null)");
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            try {
                dbConnection.close();
            } catch (SQLException closingException) {
                exception.addSuppressed(closingException);
            }
            throw exception;
        }
    }

    /** Add an entry with given name and number. Does nothing if the entry is already present. */
    public void addEntry(String name, String number) throws SQLException {
        insertPersonIfNotExists(name);
        insertPhoneIfNotExists(number);
        var updateString = "insert into Entry values " +
                           "((select Id from Person where Name = ?), (select Id from Phone where Number = ?))";
        try (var update = dbConnection.prepareStatement(updateString)) {
            update.setString(1, name);
            update.setString(2, number);
            update.execute();
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
    }

    /** Returns a list with numbers corresponding to the given name. Numbers are sorted lexicographically. */
    public List<String> getNumbersByName(String name) throws SQLException {
        var queryString = "select Number from Entry inner join Phone on " +
                          "PersonId = (select Id from Person where Name = ?) and PhoneId = Id " +
                          "order by Number";
        try (var query = dbConnection.prepareStatement(queryString)) {
            query.setString(1, name);
            var queryResult = query.executeQuery();
            var list = new ArrayList<String>();
            while (queryResult.next()) {
                list.add(queryResult.getString(1));
            }
            return list;
        }
    }

    /** Returns a list with names corresponding to the given number. Names are sorted lexicographically. */
    public List<String> getNamesByNumber(String number) throws SQLException {
        var queryString = "select Name from Entry " +
                          "inner join Person on " +
                          "PhoneId = (select Id from Phone where Number = ?) and PersonId = Id " +
                          "order by Name";
        try (var query = dbConnection.prepareStatement(queryString)) {
            query.setString(1, number);
            var queryResult = query.executeQuery();
            var list = new ArrayList<String>();
            while (queryResult.next()) {
                list.add(queryResult.getString(1));
            }
            return list;
        }
    }

    /**
     * Returns a list with entries present in the phone book. Entries are sorted by name, and then by number
     * if names is equal.
     */
    public List<Entry> getEntries() throws SQLException {
        var queryString = "select Name, Number from Entry " +
                          "inner join Person on Person.Id = PersonId " +
                          "inner join Phone on Phone.Id = PhoneId " +
                          "order by Name, Number";
        try (var query = dbConnection.prepareStatement(queryString)) {
            var queryResult = query.executeQuery();
            var list = new ArrayList<Entry>();
            while (queryResult.next()) {
                list.add(new Entry(queryResult.getString(1), queryResult.getString(2)));
            }
            return list;
        }
    }

    /** Deletes an entry with given name and number. Does nothing if no such entry exist. */
    public void deleteEntry(String name, String number) throws SQLException {
        var updateString = "delete from Entry where " +
                           "PersonId = (select Id from Person where Name = ?) and " +
                           "PhoneId = (select Id from Phone where Number = ?)";
        try (var update = dbConnection.prepareStatement(updateString)) {
            update.setString(1, name);
            update.setString(2, number);
            update.execute();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
        deleteUnreferencedPersons();
        deleteUnreferencedPhones();
        try {
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
    }

    /** Update name in entry with given name and number. Does nothing if no such entry exist. */
    public void updateName(String currentName, String currentNumber, String newName) throws SQLException {
        insertPersonIfNotExists(newName);
        var updateString = "update Entry set PersonId = (select Id from Person where Name = ?) where " +
                           "PersonId = (select Id from Person where Name = ?) and " +
                           "PhoneId = (select Id from Phone where Number = ?)";
        try (var update = dbConnection.prepareStatement(updateString)) {
            update.setString(1, newName);
            update.setString(2, currentName);
            update.setString(3, currentNumber);
            update.execute();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
        deleteUnreferencedPersons();
        try {
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
    }

    /** Update number in entry with given name and number. Does nothing if no such entry exist. */
    public void updateNumber(String currentName, String currentNumber, String newNumber) throws SQLException {
        insertPhoneIfNotExists(newNumber);
        var updateString = "update Entry set PhoneId = (select Id from Phone where Number = ?) where " +
                           "PersonId = (select Id from Person where Name = ?) and " +
                           "PhoneId = (select Id from Phone where Number = ?)";
        try (var update = dbConnection.prepareStatement(updateString)) {
            update.setString(1, newNumber);
            update.setString(2, currentName);
            update.setString(3, currentNumber);
            update.execute();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
        deleteUnreferencedPhones();
        try {
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
    }

    /** Delete all entries in the phone book. */
    public void deleteAllEntries() throws SQLException {
        try (var update = dbConnection.createStatement()) {
            update.executeUpdate("delete from Person");
            update.executeUpdate("delete from Entry");
            update.executeUpdate("delete from Phone");
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
    }

    @Override
    public void close() throws SQLException {
        dbConnection.close();
    }

    /**
     * Insert new name into Person table, if it is not already present there.
     * Note that it uses SQLite-specific clause "or ignore".
     */
    private void insertPersonIfNotExists(String name) throws SQLException {
        try (var update = dbConnection.prepareStatement("insert or ignore into Person (Name) values (?)")) {
            update.setString(1, name);
            update.execute();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
    }

    /**
     * Insert new number into Phone table, if it is not already present there.
     * Note that it uses SQLite-specific clause "or ignore".
     */
    private void insertPhoneIfNotExists(String number) throws SQLException {
        try (var update = dbConnection.prepareStatement("insert or ignore into Phone (Number) values (?)")) {
            update.setString(1, number);
            update.execute();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
    }

    /** Clean up rows in Person table that are not part of any entry anymore. */
    private void deleteUnreferencedPersons() throws SQLException {
        try (var update = dbConnection.createStatement()) {
            update.executeUpdate("delete from Person where Id not in (select PersonId from Entry)");
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
    }

    /** Clean up rows in Phone table that are not part of any entry anymore. */
    private void deleteUnreferencedPhones() throws SQLException {
        try (var update = dbConnection.createStatement()) {
            update.executeUpdate("delete from Phone where Id not in (select PhoneId from Entry)");
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressed(exception);
            throw exception;
        }
    }


    /**
     * Try to rollback in case of SQLException.
     * In case rollback() throws another exception, adds it as suppressed to {@code exception}.
     */
    private void tryToRollbackOrAddSuppressed(SQLException exception) {
        try {
            dbConnection.rollback();
        } catch (SQLException rollbackException) {
            exception.addSuppressed(rollbackException);
        }
    }

    public static class Entry {
        private final String name;
        private final String number;

        public Entry(String name, String number) {
            this.name = name;
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }
    }
}
