package com.example.threadpool;

/** Exception wrapping an exception that happened while computing inside thread pool */
public class LightExecutionException extends RuntimeException {
    public LightExecutionException(Throwable cause) {
        super(cause);
    }
}
