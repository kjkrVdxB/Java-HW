package com.example.hashtable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KeyValueListTest {
    private KeyValueList list;

    @BeforeEach
    void init() {
        list = new KeyValueList();
        list.append("aaa", "bbb");
        list.append("ccc", "ddd");
    }

    @Test
    void testAppend() {
        assertEquals("bbb", list.getHead().getNext().getValue());
        assertEquals("aaa", list.getHead().getNext().getKey());
        assertEquals("ddd", list.getHead().getValue());
        assertEquals("ccc", list.getHead().getKey());
    }

    @Test
    void testRemove() {
        list.append("aaa", "ttt");
        list.remove("aaa");
        assertEquals("ccc", list.getHead().getKey());
        assertEquals("aaa", list.getHead().getNext().getKey());
        assertNull(list.getHead().getNext().getNext());
        list.remove("aaa");
        assertNull(list.getHead().getNext());
        assertEquals("ccc", list.getHead().getKey());
        list.remove("aaa");
        assertNull(list.getHead().getNext());
        assertEquals("ccc", list.getHead().getKey());
    }

    @Test
    void testFind() {
        assertEquals("bbb", list.find("aaa").getValue());
        assertNull(list.find("aba"));
        assertEquals("ccc", list.find("ccc").getKey());
    }

    @Test
    void testRegressionWithNullKey() {
        list.append(null, "aaa");
        list.find("xxx");
    }
}
