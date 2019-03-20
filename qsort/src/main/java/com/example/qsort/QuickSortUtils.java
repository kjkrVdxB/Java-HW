package com.example.qsort;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Useful methods for all implementations of quick sort. */
class QuickSortUtils {
    /**
     * Reorders elements in a part of {@code array} from {@code l} (inclusive) to {@code r} (exclusive)
     * so that the elements lower then or equal to {@code pivot} come first, then the pivot, and then the ones
     * greater than it.
     *
     * @return the position of  the pivot in the resulting array
     */
    static <E extends Comparable<? super E>> int partition(@NonNull E[] array, int l, int r, int pivotPosition) {
        E pivot = array[pivotPosition];
        int i = l; // invariant: all elements in range [l; i - 1] are less than pivot
        int j = r - 1; // invariant: all elements in range [j + 1; r - 1] are greater than pivot
        for (int k = l; k <= j; ++k) {
            int cmp;
            while (k >= i && k <= j && (cmp = array[k].compareTo(pivot)) != 0) {
                swap(array, cmp < 0 ? i++ : j--, k);
            }
            // invariant: all elements in range [i; k] are equal to pivot
        }
        // [i; j] now represents the range of elements equal to pivot
        // return the middle so the quick sort algorithm has good probabilistic behaviour
        return (i + j) / 2;
    }

    /**
     * Sorts the part of {@code array} from {@code l} (inclusive) to {@code r} (exclusive) using insertion sort.
     * Turns out this is quite faster than Arrays.sort().
     */
    static <E extends Comparable<? super E>> void insertionSort(@NonNull E[] array, int l, int r) {
        for (int i = l; i < r; ++i) {
            for (int j = i; j > l && array[j - 1].compareTo(array[j]) > 0; --j) {
                swap(array, j - 1, j);
            }
        }
    }

    /** Swaps elements of {@code array} on positions {@code i} and {@code j}. */
    private static <T> void swap(@Nullable T @NonNull [] array, int i, int j) {
        var tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }
}
