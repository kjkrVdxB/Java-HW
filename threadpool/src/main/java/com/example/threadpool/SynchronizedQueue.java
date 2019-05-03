package com.example.threadpool;


import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

/** A simple implementation of blocking synchronized queue */
public class SynchronizedQueue<E> {
    private Element<E> start = null;
    private Element<E> end = null;
    private final Object pushLock = new Object();
    private final Object popLock = new Object();

    /**
     * Push an element
     *
     * @throws NullPointerException if the passed element is null
     */
    public void push(@NonNull E a) {
        //noinspection ResultOfMethodCallIgnored
        Validate.notNull(a);
        synchronized (pushLock) {
            if (end == null) {
                synchronized (popLock) {
                    start = end = new Element<>(a);
                    popLock.notifyAll();
                }
            } else {
                end.next = new Element<>(a);
                end = end.next;
            }
        }
    }

    /**
     * Pop an element and return it, lock until one is available
     *
     * @throws InterruptedException if the thread was interrupted while waiting for an element
     */
    @NonNull
    public E pop() throws InterruptedException {
        synchronized (popLock) {
            while (start == null) {
                popLock.wait();
            }
            var result = start.value;
            if (start.next == null) {
                synchronized (pushLock) {
                    if (start.next == null) {
                        start = end = null;
                    } else {
                        start = start.next;
                    }
                }
            } else {
                // can not read different start.next here since Element.next is volatile
                // and if we saw a non-null value it will never change
                start = start.next;
            }
            return result;
        }
    }

    private static class Element<E> {
        // volatile so pop() can see that there is next element and not grab second lock
        private volatile Element<E> next = null;
        @NonNull
        private final E value;

        private Element(@NonNull E value) {
            this.value = value;
        }
    }
}