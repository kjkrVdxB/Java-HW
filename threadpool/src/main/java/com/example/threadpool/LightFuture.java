package com.example.threadpool;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

/**
 * Interface of a future created by {@link ThreadPool}
 *
 * @param <T> type of the result of the computation represented by the future.
 */
public interface LightFuture<T> {
    /** Check that the computing finished */
    boolean isReady();

    /**
     * Get the result, blocking until it is computed.
     *
     * @throws InterruptedException if the thread was interrupted while waiting for computation
     */
    T get() throws InterruptedException;

    /**
     * Apply the function to the result of this Future after it is computed. Does not block.
     *
     * @return a new Future representing this computation, or null if the thread pool has been shut down
     * @throws NullPointerException if function is null
     */
    <U> LightFuture<U> thenApply(@NonNull Function<? super T, ? extends U> function);
}
