package com.example.qsort;

class InsertionSortTest extends SortTestBase {
    @Override
    <T extends Comparable<? super T>> void sort(T[] array) {
        QuickSortUtils.insertionSort(array, 0, array.length);
    }
}
