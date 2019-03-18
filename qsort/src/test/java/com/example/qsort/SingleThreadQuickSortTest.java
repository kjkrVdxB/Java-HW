package com.example.qsort;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SingleThreadQuickSortTest extends SortTestBase
{
    @Override
    <T extends Comparable<? super T>> void sort(T[] array) {
        SingleThreadQuickSort.quickSort(array);
    }

    @Test
    void testExceptions() {
        assertThrows(NullPointerException.class, () -> SingleThreadQuickSort.quickSort(null));
        assertThrows(IllegalArgumentException.class, () -> SingleThreadQuickSort.quickSort(new Integer[]{null, null}));
    }
}