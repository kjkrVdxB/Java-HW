package com.example.qsort;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.qsort.QuickSortUtils.insertionSort;
import static com.example.qsort.QuickSortUtils.partition;

/** Class implementing parallel quick sort using thread pool. */
public class ParallelQuickSort<E extends Comparable<? super E>> {
    private static final int INSERTION_SORT_BOUND = 5;
    private final ExecutorService executorService;
    private CountDownLatch latch;

    /**
     * Run a parallel quick sort on the {@code array}, using {@code nThreads} threads,
     * and getting randomness from {@code ThreadLocalRandom}.
     */
    public static <T extends Comparable<? super T>> void quickSort(@NonNull T[] array, int nThreads) {
        org.apache.commons.lang3.Validate.notNull(array, "Array can not be null");
        org.apache.commons.lang3.Validate.noNullElements(array, "Array can not contain null elements");

        new ParallelQuickSort<T>(nThreads).sort(array);
    }

    private ParallelQuickSort(int nThreads) {
        executorService = Executors.newFixedThreadPool(nThreads);
    }

    /** Sorts the array by running the first task and waiting for the latch. */
    private void sort(@NonNull E[] array) {
        latch = new CountDownLatch(array.length);
        executorService.submit(() -> sort(array, 0, array.length));
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
        executorService.shutdownNow();
    }

    /**
     * Sorts the part of {@code array} from {@code l} (inclusive) to {@code r} (exclusive) by partitioning it
     * and then running a separate task on the second part while recursively solving the first part in current thread.
     */
    private void sort(@NonNull E[] array, int l, int r) {
        if (r - l <= INSERTION_SORT_BOUND) {
            insertionSort(array, l, r);
            for (int i = 0; i < r - l; ++i) {
                latch.countDown();
            }
            return;
        }
        int pivotPosition = ThreadLocalRandom.current().nextInt(r - l) + l;
        int newPivotPosition = partition(array, l, r, pivotPosition);
        latch.countDown(); // the pivot will not participate in further sorting
        executorService.submit(() -> sort(array, newPivotPosition + 1, r));
        sort(array, l, newPivotPosition);
    }
}
