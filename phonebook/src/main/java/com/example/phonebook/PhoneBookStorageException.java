package com.example.phonebook;

/** Exception representing phone book storage failure */
public class PhoneBookStorageException extends RuntimeException {
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