package com.example.hashtable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyValueListTest {
    private KeyValueList testList;

    @BeforeEach
    void init() {
        testList = new KeyValueList();
        testList.append(new KeyValueList.Entry("aaa", "bbb"));
        testList.append(new KeyValueList.Entry("ccc", "ddd"));
    }

    @Test
    void testAppendPopBack() {
        var last = testList.popFront();

        assertEquals("ddd", last.getValue());
        assertEquals("ccc", last.getKey());

        last = testList.popFront();

        assertEquals("bbb", last.getValue());
        assertEquals("aaa", last.getKey());

        assertNull(testList.popFront());
    }

    @Test
    void testRemove() {
        testList.append(new KeyValueList.Entry("aaa", "ttt"));
        testList.remove("aaa");
        assertEquals("ccc", testList.popFront().getKey());
        assertEquals("aaa", testList.popFront().getKey());
        assertDoesNotThrow(() -> testList.remove("aaa"));
    }

    @Test
    void testFind() {
        assertEquals("bbb", testList.find("aaa").getValue());
        assertNull(testList.find("aba"));
        assertEquals("ccc", testList.find("ccc").getKey());
    }

    @Test
    void testRegressionWithNullKey() {
        testList.append(new KeyValueList.Entry(null, "aaa"));
        assertDoesNotThrow(() -> testList.find("xxx"));
    }
}
