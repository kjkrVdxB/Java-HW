package com.example.qsort;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ParallelQuickSortTest extends SortTestBase {
    @Override
    <T extends Comparable<? super T>> void sort(T[] array) {
        ParallelQuickSort.quickSort(array, 2);
    }

    @Test
    void testExceptions() {
        assertThrows(NullPointerException.class, () -> ParallelQuickSort.quickSort(null, 2));
        assertThrows(IllegalArgumentException.class, () -> ParallelQuickSort.quickSort(new Integer[]{null, null}, 2));
        assertThrows(IllegalArgumentException.class, () -> ParallelQuickSort.quickSort(new Integer[]{1, 2}, -7));
    }
}