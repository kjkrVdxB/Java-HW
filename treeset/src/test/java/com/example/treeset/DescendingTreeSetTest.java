package com.example.treeset;

import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;

class DescendingTreeSetTest extends TreeSetTest {
    @Override
    @BeforeEach
    void init() {
        testSet = (new TreeSet<String>(Collections.reverseOrder())).descendingSet();
        testSet.add("2");
        testSet.add("6");
        testSet.add("0");
        testSet.add("4");
        testSet.add("8");
    }
}