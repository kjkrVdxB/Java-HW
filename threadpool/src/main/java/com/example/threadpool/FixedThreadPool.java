package com.example.threadpool;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/** A thread pool with a fixed number of worker threads */
public class FixedThreadPool implements ThreadPool {
    private final SynchronizedQueue<Task<?>> taskQueue = new SynchronizedQueue<>();
    private final Thread[] workerThreads;
    private volatile boolean shutDown = false;

    /** Create a new thread pool with the given number of worker threads */
    public FixedThreadPool(int nThreads) {
        workerThreads = new Thread[nThreads];
        for (int i = 0; i < nThreads; ++i) {
            workerThreads[i] = new Thread(new Worker());
            workerThreads[i].setDaemon(true);
            workerThreads[i].start();
        }
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    var task = taskQueue.pop();
                    task.compute();
                    synchronized (task.thenApplyList) {
                        for (var thenTask: task.thenApplyList) {
                            taskQueue.push(thenTask);
                        }
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T> LightFuture<T> submit(@NonNull Supplier<? extends T> supplier) {
        //noinspection ResultOfMethodCallIgnored
        Validate.notNull(supplier);
        if (shutDown) {
            return null;
        }
        var task = new Task<T>(supplier);
        taskQueue.push(task);
        return task;
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
        // kind of like a singleton, so that multiple threads doing shutdown()
        // don't wait for each other
        if (!shutDown) {
            synchronized (workerThreads) {
                if (!shutDown) {
                    shutDown = true;
                    for (var workerThread: workerThreads) {
                        workerThread.interrupt();
                    }
                    // NB: we do not clean up the queue (and associated tasks in thenApplyLists) for the following
                    // reasons:
                    // * it is not explicitly required by the assignment
                    // * it would considerably complicate logic of submit/shutdown/workers
                    //   + particularly a worker pops a new tasks, but there is no way for shutdown to find the
                    //     thenApplyList of this task, except for waiting for workers (which shutdown should not do
                    //     and the worker could actually be stuck with a task that never ends, which should not prevent
                    //     the tasks in thenApplyList from being discarded), or maintaining all the tasks in a separate
                    //     list, which would be under another lock, or the workers saving the tasks they compute
                    //     somewhere else, but that would need a lock too since the workers need to pop+save atomically
                    // * it would introduce locks common for workers/submit/shutdown thus prevent them from
                    //   working concurrently
                    // * this would make the discarded tasks `ready` with an exception propagated to all get()s waiting
                    //   for it, but the name LightExecutionException does not imply this use case, and the assignment
                    //   does not specify any other new exception types and behaviors for get()
                }
            }
        }
    }

    /** A class representing both task in the queue and a LightFuture associated with it */
    private class Task<T> implements LightFuture<T> {
        private final Supplier<? extends T> supplier;
        private final List<Task> thenApplyList = new LinkedList<>();
        private T result = null;
        private volatile boolean ready = false;
        private RuntimeException computationException = null;

        private Task(Supplier<? extends T> supplier) {
            this.supplier = supplier;
        }

        /** Compute the result and notify all waiting for it */
        private synchronized void compute() {
            try {
                result = supplier.get();
            } catch (RuntimeException e) {
                computationException = e;
            }
            ready = true;
            notifyAll();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isReady() {
            // not synchronized and uses the fact that 'ready' is volatile
            return ready;
        }

        /** {@inheritDoc} */
        @Override
        public T get() throws InterruptedException {
            if (!ready) {
                synchronized (this) {
                    while (!ready) {
                        wait();
                    }
                }
            }
            if (computationException != null) {
                throw new LightExecutionException(computationException);
            }
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public <U> LightFuture<U> thenApply(@NonNull Function<? super T, ? extends U> function) {
            //noinspection ResultOfMethodCallIgnored
            Validate.notNull(function);
            if (shutDown) {
                return null;
            }
            LightFuture<U> newTask;
            synchronized (thenApplyList) {
                if (ready) {
                    newTask = submit(() -> function.apply(result));
                } else {
                    newTask = new Task<>(() -> function.apply(result));
                    thenApplyList.add((Task) newTask);
                }
            }
            return newTask;
        }
    }
}
