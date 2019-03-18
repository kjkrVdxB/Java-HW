package com.example.qsort;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.example.qsort.QuickSortUtils.partition;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartitionTest {
    @Test
    void testPartitionAllEqual() {
        checkPartitioningProcess(ArrayReorderingTestUtils.ARRAY_OF_50_SAME_INTEGERS, 20);
    }

    @Test
    void testPartitionDecreasing() {
        checkPartitioningProcess(ArrayReorderingTestUtils.ARRAY_OF_50_DECREASING_INTEGERS, 40);
    }

    @Test
    void testPartitionAlternating() {
        checkPartitioningProcess(ArrayReorderingTestUtils.ARRAY_OF_50_ALTERNATING_INTEGERS, 10);
    }

    void checkPartitioningProcess(Integer[] array, int pivotPosition) {
        var partition = Arrays.copyOf(array, array.length);
        int bound = partition(partition, 0, array.length, pivotPosition);
        checkIsPartition(array, partition, bound);
    }

    void checkIsPartition(Integer[] array, Integer[] probablyPartition, int bound) {
        var arrayCopy = Arrays.copyOf(array, array.length);
        var partitionCopy = Arrays.copyOf(probablyPartition, probablyPartition.length);
        Arrays.sort(arrayCopy);
        Arrays.sort(partitionCopy);
        assertArrayEquals(arrayCopy, partitionCopy);
        for (int i = 0; i < probablyPartition.length; ++i) {
            if (i < bound) {
                assertTrue(probablyPartition[i] <= probablyPartition[bound]);
            } else {
                assertTrue(probablyPartition[i] >= probablyPartition[bound]);
            }
        }
    }
}