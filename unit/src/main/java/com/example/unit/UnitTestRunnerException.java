package com.example.unit;

public class UnitTestRunnerException extends RuntimeException {
    public UnitTestRunnerException() {
    }

    public UnitTestRunnerException(String message) {
        super(message);
    }

    public UnitTestRunnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnitTestRunnerException(Throwable cause) {
        super(cause);
    }
}
