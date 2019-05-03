package com.example.threadpool;

import java.util.function.Supplier;

/** interface of a simple thread pool */
public interface ThreadPool {
    /**
     * Submit a task for execution.
     *
     * @return A LightFuture representing the submitted task, or null if the pool has been shut down
     * @throws NullPointerException if supplier is null
     */
    <T> LightFuture<T> submit(Supplier<? extends T> supplier);

    /** Shut down the pool. Try to stop current tasks by interrupting them and wait for their completion. */
    void shutdown();
}
