package com.example.hashtable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KeyValueListTest {
    private KeyValueList testList;

    @BeforeEach
    void init() {
        testList = new KeyValueList();
        testList.append("aaa", "bbb");
        testList.append("ccc", "ddd");
    }

    @Test
    void testAppend() {
        assertEquals("bbb", testList.getHead().getNext().getValue());
        assertEquals("aaa", testList.getHead().getNext().getKey());
        assertEquals("ddd", testList.getHead().getValue());
        assertEquals("ccc", testList.getHead().getKey());
    }

    @Test
    void testRemove() {
        testList.append("aaa", "ttt");
        testList.remove("aaa");
        assertEquals("ccc", testList.getHead().getKey());
        assertEquals("aaa", testList.getHead().getNext().getKey());
        assertNull(testList.getHead().getNext().getNext());
        testList.remove("aaa");
        assertNull(testList.getHead().getNext());
        assertEquals("ccc", testList.getHead().getKey());
        testList.remove("aaa");
        assertNull(testList.getHead().getNext());
        assertEquals("ccc", testList.getHead().getKey());
    }

    @Test
    void testFind() {
        assertEquals("bbb", testList.find("aaa").getValue());
        assertNull(testList.find("aba"));
        assertEquals("ccc", testList.find("ccc").getKey());
    }

    @Test
    void testRegressionWithNullKey() {
        testList.append(null, "aaa");
        testList.find("xxx");
    }
}
