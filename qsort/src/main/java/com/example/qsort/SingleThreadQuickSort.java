package com.example.qsort;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.ThreadLocalRandom;

import static com.example.qsort.QuickSortUtils.insertionSort;
import static com.example.qsort.QuickSortUtils.partition;

/** Class implementing single-thread quick sort. */
public class SingleThreadQuickSort {
    private static final int INSERTION_SORT_BOUND = 5; /* equal to that of ParallelQuickSort for the sake of comparison,
                                                          but ideally should be tuned for best performance so is
                                                          separate. */

    /** Sorts the {@code array} using single-thread quick sort. */
    public static <T extends Comparable<? super T>> void quickSort(@NonNull T[] array) {
        org.apache.commons.lang3.Validate.notNull(array, "Array can not be null");
        org.apache.commons.lang3.Validate.noNullElements(array, "Array can not contain null elements");

        sort(array, 0, array.length);
    }

    /**
     * Sorts the part of {@code array} from {@code l} (inclusive) to {@code r} (exclusive) by partitioning it
     * and then sequentially runs recursive calls on the first and second part.
     */
    private static <T extends Comparable<? super T>> void sort(@NonNull T[] array, int l, int r) {
        if (r - l <= INSERTION_SORT_BOUND) {
            insertionSort(array, l, r);
            return;
        }
        int pivotPosition = ThreadLocalRandom.current().nextInt(r - l) + l;
        int newPivotPosition = partition(array, l, r, pivotPosition);
        sort(array, l, newPivotPosition);
        sort(array, newPivotPosition + 1, r);
    }
}
