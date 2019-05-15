package com.example.test3;

/** An exception that is throw by directory hash computing routines */
public class DirectoryHashComputingException extends RuntimeException {
    public DirectoryHashComputingException() {
    }

    public DirectoryHashComputingException(String message) {
        super(message);
    }

    public DirectoryHashComputingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DirectoryHashComputingException(Throwable cause) {
        super(cause);
    }
}
