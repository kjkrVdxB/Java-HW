package com.example.qsort;

import java.util.stream.Stream;

class ArrayReorderingTestUtils {
    static final Integer[] ARRAY_OF_50_SAME_INTEGERS =
            Stream.generate(() -> 2).limit(50).toArray(Integer[]::new);
    static final Integer[] ARRAY_OF_50_DECREASING_INTEGERS =
            Stream.iterate(50, n -> n - 1).limit(50).toArray(Integer[]::new);
    /** 40, -39, 38, -37, ..., 2, -1 */
    static final Integer[] ARRAY_OF_50_ALTERNATING_INTEGERS =
            Stream.iterate(50, n -> (n < 0 ? -1 : 1) - n).limit(50).toArray(Integer[]::new);
}
