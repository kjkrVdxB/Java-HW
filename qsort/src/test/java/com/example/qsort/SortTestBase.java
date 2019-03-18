package com.example.qsort;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

abstract class SortTestBase {
    abstract <T extends Comparable<? super T>> void sort(T[] array);

    @Test
    void testAlternatingArray() {
        checkSort(ArrayReorderingTestUtils.ARRAY_OF_50_ALTERNATING_INTEGERS);
    }

    @Test
    void testAllEqualElements() {
        checkSort(ArrayReorderingTestUtils.ARRAY_OF_50_SAME_INTEGERS);
    }

    @Test
    void testDecreasingArray() {
        checkSort(ArrayReorderingTestUtils.ARRAY_OF_50_DECREASING_INTEGERS);
    }

    private void checkSort(Integer[] array) {
        var arrayCopy = Arrays.copyOf(array, array.length);
        Arrays.sort(arrayCopy);
        sort(array);
        assertArrayEquals(arrayCopy, array);
    }
}
