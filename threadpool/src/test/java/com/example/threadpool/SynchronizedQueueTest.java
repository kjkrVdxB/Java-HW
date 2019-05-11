package com.example.threadpool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CyclicBarrier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SynchronizedQueueTest {
    private volatile SynchronizedQueue<String> queue;

    @BeforeEach
    void init() {
        queue = new SynchronizedQueue<>();
    }

    @Test
    void testSingleThread() throws InterruptedException {
        queue.push("1");
        queue.push("2");
        assertEquals("1", queue.pop());
        queue.push("3");
        assertEquals("2", queue.pop());
        assertEquals("3", queue.pop());
    }

    @RepeatedTest(10)
    void testMultipleThreads() throws InterruptedException {
        int threadCount = 100;

        var barrier = new CyclicBarrier(threadCount * 2);

        var threads = new ArrayList<Thread>();

        var results = new ArrayList<String>();

        for (int i = 0; i < threadCount; ++i) {
            int j = i;
            var pushThread = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception ignored) {

                }
                queue.push("" + j);
            });
            threads.add(pushThread);
            pushThread.start();
            var popThread = new Thread(() -> {
                String result = null;
                try {
                    barrier.await();
                    result = queue.pop();
                } catch (Exception ignored) {
                }
                synchronized (results) {
                    results.add(result);
                }
            });
            threads.add(popThread);
            popThread.start();
        }

        for (var thread: threads) {
            thread.join();
        }

        assertEquals(threadCount, results.size());
        results.sort(Comparator.comparing(Integer::valueOf));
        for (int i = 0; i < threadCount; ++i) {
            assertEquals("" + i, results.get(i));
        }
    }

    @Test
    void testPushNullElementException() {
        //noinspection ConstantConditions
        assertThrows(NullPointerException.class, () -> queue.push(null));
    }
}