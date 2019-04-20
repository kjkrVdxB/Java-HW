package com.example.threadpool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FixedThreadPoolTest {
    private static final int REPEAT_CONCURRENCY_TESTS_COUNT = 10;
    private ThreadPool poolWithFourThreads;

    @BeforeEach
    void init() {
        poolWithFourThreads = new FixedThreadPool(4);
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testBasicGet() throws InterruptedException {
        var future = poolWithFourThreads.submit(() -> 1);
        assertEquals(1, (int) future.get());
        assertEquals(1, (int) future.get());
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testIsReady() throws InterruptedException {
        var lock = new Object();
        LightFuture<Integer> future;
        synchronized (lock) {
            future = poolWithFourThreads.submit(() -> {
                synchronized (lock) {
                    return 1;
                }
            });
            assertFalse(future.isReady());
        }
        future.get();
        assertTrue(future.isReady());
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testThenApplyBeforeFinished() throws InterruptedException {
        var lock = new Object();
        LightFuture<Integer> future1;
        LightFuture<Integer> future2;

        synchronized (lock) {
            future1 = poolWithFourThreads.submit(() -> {
                synchronized (lock) {
                    return 1;
                }
            });
            future2 = future1.thenApply(a -> a * 2);
        }

        assertEquals(1, (int) future1.get());
        assertEquals(2, (int) future2.get());
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testThenApplyAfterFinished() throws InterruptedException {
        var future1 = poolWithFourThreads.submit(() -> 1);
        assertEquals(1, (int) future1.get());
        var future2 = future1.thenApply(a -> a * 2);
        assertEquals(2, (int) future2.get());
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testCanNotSubmitAfterShutdown() {
        poolWithFourThreads.shutdown();
        assertNull(poolWithFourThreads.submit(() -> 1));
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testCanNotThenApplyAfterShutdown() {
        var future = poolWithFourThreads.submit(() -> 1);
        assertNotNull(future);
        poolWithFourThreads.shutdown();
        assertNull(future.thenApply(a -> a * 2));
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testThenApplyDoesNotBlock() throws InterruptedException {
        var lock = new Object();
        LightFuture<Integer> thenApplyFuture;
        synchronized (lock) {
            var future = poolWithFourThreads.submit(() -> {
                synchronized (lock) {
                    return 1;
                }
            });
            thenApplyFuture = future.thenApply(a -> a * 2);
            // if thenApply blocked we could not leave this sync block
        }
        assertEquals(2, (int) thenApplyFuture.get());
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testComputationException() {
        var future = poolWithFourThreads.submit(() -> {
            throw new NullPointerException();
        });
        try {
            future.get();
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            assertTrue(e instanceof LightExecutionException);
            assertTrue(e.getCause() instanceof NullPointerException);
        }
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testShutdownComputationIsInterrupted() throws InterruptedException {
        var latch = new CountDownLatch(1);
        var future = poolWithFourThreads.submit(() -> {
            latch.countDown();
            try {
                var lock = new Object();
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                return 1;
            }
            return 2;
        });
        latch.await();
        poolWithFourThreads.shutdown();
        assertEquals(1, (int) future.get());
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testAtLeastFourThreads() throws InterruptedException {
        var computationsCount = 20; // multiple of 4, number of worker threads

        var results = new int[computationsCount];
        var futures = new ArrayList<LightFuture<Void>>();
        var barrier = new CyclicBarrier(4);

        for (int i = 0; i < computationsCount; ++i) {
            int finalI = i;
            futures.add(poolWithFourThreads.submit(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException ignored) {
                }
                results[finalI] = finalI * finalI;
                return null;
            }));
        }

        for (var future: futures) {
            future.get();
        }

        for (int i = 0; i < computationsCount; ++i) {
            assertEquals(i * i, results[i]);
        }
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testNotMoreThanFourThreads() throws InterruptedException {
        var computationsCount = 10;

        var results = new int[computationsCount];
        var futures = new ArrayList<LightFuture<Void>>();
        var semaphore = new Semaphore(4);

        for (int i = 0; i < computationsCount; ++i) {
            int finalI = i;
            futures.add(poolWithFourThreads.submit(() -> {
                try {
                    assertTrue(semaphore.tryAcquire());
                    Thread.sleep(50);
                    semaphore.release();
                } catch (InterruptedException ignored) {
                }
                results[finalI] = finalI * finalI;
                return null;
            }));
        }

        for (var future: futures) {
            future.get();
        }

        for (int i = 0; i < computationsCount; ++i) {
            assertEquals(i * i, results[i]);
        }
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testManyTasksSubmittedSimultaneously() throws InterruptedException {
        var computationsCount = 20;

        var results = new int[computationsCount];
        var futures = new LightFuture[computationsCount];

        startSimultaneouslyAndWait(Stream.iterate(0, n -> n + 1).limit(computationsCount).map(i -> (Runnable) () -> {
            futures[i] = poolWithFourThreads.submit(() -> {
                results[i] = i * i;
                return null;
            });
        }).collect(Collectors.toList()));

        for (var future: futures) {
            future.get();
        }

        for (int i = 0; i < computationsCount; ++i) {
            assertEquals(i * i, results[i]);
        }
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testConcurrentGetInProgress() throws InterruptedException {
        var gettersCount = 20;

        var barrier = new CyclicBarrier(gettersCount + 1);
        var future = poolWithFourThreads.submit(() -> {
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException ignored) {
            }
            return 1;
        });

        startSimultaneouslyAndWait(Stream.iterate(0, n -> n + 1).limit(gettersCount).map(i -> (Runnable) () -> {
            try {
                assertEquals(1, (int) future.get());
            } catch (InterruptedException ignored) {
            }
        }).collect(Collectors.toList()), barrier);
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testConcurrentGetCompleted() throws InterruptedException {
        var pool = new FixedThreadPool(1);
        var gettersCount = 20;

        var barrier = new CyclicBarrier(gettersCount + 1);
        var future = pool.submit(() -> 1);
        // Note: here we put a blocking task that guarantees that the first one finished when the barrier breaks
        pool.submit(() -> {
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException ignored) {
            }
            return 44;
        });

        startSimultaneouslyAndWait(Stream.iterate(0, n -> n + 1).limit(gettersCount).map(i -> (Runnable) () -> {
            try {
                assertEquals(1, (int) future.get());
            } catch (InterruptedException ignored) {
            }
        }).collect(Collectors.toList()), barrier);
    }

    @RepeatedTest(REPEAT_CONCURRENCY_TESTS_COUNT)
    void testOneThreadPoolFinishesTasksInOrder() throws InterruptedException {
        var tasksCount = 50;

        var pool = new FixedThreadPool(1);

        var finished = new boolean[tasksCount];
        var futures = new ArrayList<LightFuture<Void>>();

        for (int i = 0; i < tasksCount; ++i) {
            int finalI = i;
            futures.add(pool.submit(() -> {
                synchronized (finished) {
                    assertTrue(finalI == 0 || finished[finalI - 1]);
                    finished[finalI] = true;
                }
                return null;
            }));
        }

        for (var future: futures) {
            future.get();
        }
    }

    @Test
    void testNullSupplierException() {
        assertThrows(NullPointerException.class, () -> poolWithFourThreads.submit(null));
    }

    @Test
    void testNullThenApplyFunctionException() {
        var future = poolWithFourThreads.submit(() -> 1);
        assertThrows(NullPointerException.class, () -> future.thenApply(null));
    }

    private void startSimultaneouslyAndWait(List<Runnable> runnableList, CyclicBarrier barrier) throws InterruptedException {
        var threads = new ArrayList<Thread>();

        for (var runnable: runnableList) {
            var thread = new Thread(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException ignored) {
                }
                runnable.run();
            });
            threads.add(thread);
            thread.start();
        }

        for (var thread: threads) {
            thread.join();
        }
    }

    private void startSimultaneouslyAndWait(List<Runnable> runnableList) throws InterruptedException {
        startSimultaneouslyAndWait(runnableList, new CyclicBarrier(runnableList.size()));
    }
}