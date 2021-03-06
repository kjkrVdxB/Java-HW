package com.example.phonebook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Phone book implementation backed by SQLite database.
 * <p>
 * Constructor and all methods throw {@code PhoneBookStorageException} in case of a database failure.
 * Constructor and all methods throw {@code IllegalArgumentException} in case any of {@code String} parameters
 * is {@code null}.
 */
public class PhoneBook implements AutoCloseable {
    private final Connection dbConnection;

    private static final String STORAGE_EXCEPTION_MESSAGE = "Phone book storage experienced an unrecoverable error";

    /**
     * Create new PhoneBook with an open connection to database file named {@code dbPath}. Path can be anything that
     * SQLite-jdbc library accepts in connection string after 'jdbc:sqlite:', for example ':memory:'.
     */
    public PhoneBook(String dbPath) {
        if (dbPath == null) {
            throw new IllegalArgumentException("Null dbPath is prohibited");
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            connection.setAutoCommit(false);
        } catch (SQLException exception) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closingException) {
                    exception.addSuppressed(closingException);
                }
            }
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
        dbConnection = connection;
        try (var create = dbConnection.createStatement()) {
            create.executeUpdate("create table if not exists Person " +
                                 "(Id integer primary key asc, Name text unique not null)");
            create.executeUpdate("create table if not exists Entry " +
                                 "(PersonId integer not null, PhoneId integer not null, unique(PersonId, PhoneId))");
            create.executeUpdate("create table if not exists Phone " +
                                 "(Id integer primary key asc, Number text unique not null)");
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            try {
                dbConnection.close();
            } catch (SQLException closingException) {
                exception.addSuppressed(closingException);
            }
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /** Add an entry with given name and number. Does nothing if the entry is already present. */
    public void addEntry(String name, String number) {
        if (name == null) {
            throw new IllegalArgumentException("Null name is prohibited");
        }
        if (number == null) {
            throw new IllegalArgumentException("Null number is prohibited");
        }
        insertPersonIfNotExists(name);
        insertPhoneIfNotExists(number);
        // Note that here we use SQLite-specific clause "or ignore"
        var updateString = "insert or ignore into Entry values " +
                           "((select Id from Person where Name = ?), (select Id from Phone where Number = ?))";
        try (var update = dbConnection.prepareStatement(updateString)) {
            update.setString(1, name);
            update.setString(2, number);
            update.execute();
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /** Returns a list with numbers corresponding to the given name. Numbers are sorted lexicographically. */
    public List<String> getNumbersByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Null name is prohibited");
        }
        var queryString = "select Number from Entry inner join Phone on " +
                          "PersonId = (select Id from Person where Name = ?) and PhoneId = Id " +
                          "order by Number";
        return executeQueryWithOneStringParameterAndReturningStrings(queryString, name);
    }

    /** Returns a list with names corresponding to the given number. Names are sorted lexicographically. */
    public List<String> getNamesByNumber(String number) {
        if (number == null) {
            throw new IllegalArgumentException("Null number is prohibited");
        }
        var queryString = "select Name from Entry " +
                          "inner join Person on " +
                          "PhoneId = (select Id from Phone where Number = ?) and PersonId = Id " +
                          "order by Name";
        return executeQueryWithOneStringParameterAndReturningStrings(queryString, number);
    }

    private List<String> executeQueryWithOneStringParameterAndReturningStrings(String queryString, String parameter) {
        assert queryString != null;
        assert parameter != null;

        try (var query = dbConnection.prepareStatement(queryString)) {
            query.setString(1, parameter);
            var queryResult = query.executeQuery();
            var list = new ArrayList<String>();
            while (queryResult.next()) {
                list.add(queryResult.getString(1));
            }
            return list;
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /**
     * Returns a list with entries present in the phone book. Entries are sorted by name, and then by number
     * if names are equal.
     */
    public List<Entry> getEntries() {
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
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /** Returns true if the entry is in the phone book. */
    public boolean containsEntry(String name, String number) {
        if (name == null) {
            throw new IllegalArgumentException("Null name is prohibited");
        }
        if (number == null) {
            throw new IllegalArgumentException("Null number is prohibited");
        }
        var queryString = "select * from Entry where " +
                          "PersonId = (select Id from Person where Name = ?) and " +
                          "PhoneId = (select Id from Phone where Number = ?)";
        try (var query = dbConnection.prepareStatement(queryString)) {
            query.setString(1, name);
            query.setString(2, number);
            var queryResult = query.executeQuery();
            return queryResult.next(); // true only if the ResultSet contains at least one element
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /** Deletes an entry with given name and number. Does nothing if no such entry exist. */
    public void deleteEntry(String name, String number) {
        if (name == null) {
            throw new IllegalArgumentException("Null name is prohibited");
        }
        if (number == null) {
            throw new IllegalArgumentException("Null number is prohibited");
        }
        var updateString = "delete from Entry where " +
                           "PersonId = (select Id from Person where Name = ?) and " +
                           "PhoneId = (select Id from Phone where Number = ?)";
        try (var update = dbConnection.prepareStatement(updateString)) {
            update.setString(1, name);
            update.setString(2, number);
            update.execute();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
        deleteUnreferencedPersons();
        deleteUnreferencedPhones();
        try {
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /**
     * Update name in entry with given name and number. Does nothing if no such entry exist. If the operation
     * creates an entry equivalent to an already existing entry, they are merged.
     */
    public void updateName(String currentName, String currentNumber, String newName) {
        if (currentName == null) {
            throw new IllegalArgumentException("Null currentName is prohibited");
        }
        if (currentNumber == null) {
            throw new IllegalArgumentException("Null currentNumber is prohibited");
        }
        if (newName == null) {
            throw new IllegalArgumentException("Null newName is prohibited");
        }
        insertPersonIfNotExists(newName);
        // Note that here we use SQLite-specific clause "or replace"
        var updateString = "update or replace Entry set PersonId = (select Id from Person where Name = ?) where " +
                           "PersonId = (select Id from Person where Name = ?) and " +
                           "PhoneId = (select Id from Phone where Number = ?)";
        try (var update = dbConnection.prepareStatement(updateString)) {
            update.setString(1, newName);
            update.setString(2, currentName);
            update.setString(3, currentNumber);
            update.execute();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
        deleteUnreferencedPersons();
        try {
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /**
     * Update number in entry with given name and number. Does nothing if no such entry exist. If the operation
     * creates an entry equivalent to an already existing entry, they are merged.
     */
    public void updateNumber(String currentName, String currentNumber, String newNumber) {
        if (currentName == null) {
            throw new IllegalArgumentException("Null currentName is prohibited");
        }
        if (currentNumber == null) {
            throw new IllegalArgumentException("Null currentNumber is prohibited");
        }
        if (newNumber == null) {
            throw new IllegalArgumentException("Null newNumber is prohibited");
        }
        insertPhoneIfNotExists(newNumber);
        // Note that here we use SQLite-specific clause "or replace"
        var updateString = "update or replace Entry set PhoneId = (select Id from Phone where Number = ?) where " +
                           "PersonId = (select Id from Person where Name = ?) and " +
                           "PhoneId = (select Id from Phone where Number = ?)";
        try (var update = dbConnection.prepareStatement(updateString)) {
            update.setString(1, newNumber);
            update.setString(2, currentName);
            update.setString(3, currentNumber);
            update.execute();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
        deleteUnreferencedPhones();
        try {
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /** Delete all entries in the phone book. */
    public void deleteAllEntries() {
        try (var update = dbConnection.createStatement()) {
            update.executeUpdate("delete from Person");
            update.executeUpdate("delete from Entry");
            update.executeUpdate("delete from Phone");
            dbConnection.commit();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    @Override
    public void close() {
        try {
            dbConnection.close();
        } catch (SQLException exception) {
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /**
     * Insert new name into Person table, if it is not already present there.
     */
    private void insertPersonIfNotExists(String name) {
        assert name != null;

        // Note that here we use SQLite-specific clause "or ignore"
        try (var update = dbConnection.prepareStatement("insert or ignore into Person (Name) values (?)")) {
            update.setString(1, name);
            update.execute();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /**
     * Insert new number into Phone table, if it is not already present there.
     */
    private void insertPhoneIfNotExists(String number) {
        assert number != null;

        // Note that here we use SQLite-specific clause "or ignore"
        try (var update = dbConnection.prepareStatement("insert or ignore into Phone (Number) values (?)")) {
            update.setString(1, number);
            update.execute();
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /** Clean up rows in Person table that are not part of any entry anymore. */
    private void deleteUnreferencedPersons() {
        try (var update = dbConnection.createStatement()) {
            update.executeUpdate("delete from Person where Id not in (select PersonId from Entry)");
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }

    /** Clean up rows in Phone table that are not part of any entry anymore. */
    private void deleteUnreferencedPhones() {
        try (var update = dbConnection.createStatement()) {
            update.executeUpdate("delete from Phone where Id not in (select PhoneId from Entry)");
        } catch (SQLException exception) {
            tryToRollbackOrAddSuppressedTo(exception);
            throw new PhoneBookStorageException(STORAGE_EXCEPTION_MESSAGE, exception);
        }
    }


    /**
     * Try to rollback in case of SQLException.
     * In case rollback() throws another exception, adds it as suppressed to {@code exception}.
     */
    private void tryToRollbackOrAddSuppressedTo(SQLException exception) {
        assert exception != null;

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
            assert name != null;
            assert number != null;

            this.name = name;
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj.getClass() == Entry.class) {
                Entry other = (Entry) obj;
                return name.equals(other.name) && number.equals(other.number);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, number);
        }

        @Override
        public String toString() {
            return "Entry{name: " + name + ", number: " + number + "}";
        }
    }
}
