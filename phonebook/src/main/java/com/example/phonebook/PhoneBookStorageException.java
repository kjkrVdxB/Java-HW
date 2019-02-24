package com.example.phonebook;

public class PhoneBookStorageException extends Exception {
    public PhoneBookStorageException() { }

    public PhoneBookStorageException(String message) {
        super(message);
    }

    public PhoneBookStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhoneBookStorageException(Throwable cause) {
        super(cause);
    }

    protected PhoneBookStorageException(String message, Throwable cause, boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}