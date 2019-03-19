package com.example.qsort;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Random;
import java.util.stream.Stream;

public class Main {
    private static final int RUNS_FOR_COMPUTING_AVERAGE_TIME = 8;
    private static final int ELEMENTS_STRINGS_PREFIX_LENGTH = 100000;
    private static final int ELEMENTS_COUNT_START = 10;
    private static final int ELEMENTS_COUNT_END = 2560;
    private static final int ELEMENTS_COUNT_STEP_MULTIPLIER = 2;
    private static final int SEED = 209;

    public static void main(String[] args) {
        for (int threadsCount = 1; threadsCount <= 4; ++threadsCount) {
            System.out.println(threadsCount == 1 ? "Testing single-threaded qsort"
                                                 : "Testing parallel qsort with " + threadsCount + " threads");
            for (int elementsCount = ELEMENTS_COUNT_START;
                 elementsCount <= ELEMENTS_COUNT_END;
                 elementsCount *= ELEMENTS_COUNT_STEP_MULTIPLIER) {
                var random = new Random(SEED);
                var elementsCountFinal = elementsCount;
                var threadsCountFinal = threadsCount;
                Runnable sortingTask = threadsCount == 1
                                       ? () -> SingleThreadQuickSort.quickSort(generateStrings(elementsCountFinal,
                                                                                                random))
                                       : () -> ParallelQuickSort.quickSort(generateStrings(elementsCountFinal,
                                                                                            random),
                                                                           threadsCountFinal);
                System.out.println(elementsCount + " elements: " + averageTime(sortingTask) + " ms");
            }
            System.out.println();
        }
    }

    /** Returns average time the runnable took to run in milliseconds */
    private static double averageTime(@NonNull Runnable runnable) {
        double averageTime = 0;
        for (int i = 0; i < 2 * RUNS_FOR_COMPUTING_AVERAGE_TIME; ++i) {
            long timeMillis = time(runnable);
            // first RUN_FOR_COMPUTING_AVERAGE_TIME runs are not counted so the JIT can learn
            if (i >= RUNS_FOR_COMPUTING_AVERAGE_TIME) {
                averageTime += timeMillis;
            }
        }
        return averageTime * 1d / RUNS_FOR_COMPUTING_AVERAGE_TIME;
    }

    /** Returns time the runnable took to run in milliseconds */
    private static long time(@NonNull Runnable runnable) {
        long startTime = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - startTime;
    }

    /** Generates an array of random integers using given Random instance */
    private static String[] generateStrings(int count, @NonNull Random random) {
        return Stream.generate(random::nextInt)
                .map(i -> "a".repeat(ELEMENTS_STRINGS_PREFIX_LENGTH) + i)
                .limit(count)
                .toArray(String[]::new);
    }
}
