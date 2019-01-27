package com.example.hashtable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashTableTest {
    private HashTable testTable;

    @BeforeEach
    void initTable() {
        testTable = new HashTable();
    }

    @Test
    void testSize() {
        assertEquals(0, testTable.size());

        testTable.put("aaa", "bbb");

        assertEquals(1, testTable.size());
    }

    @Test
    void testPutAndContains() {
        String previous = testTable.put("aaa", "bbb");

        assertNull(previous);
        assertTrue(testTable.contains("aaa"));
        assertFalse(testTable.contains("xxx"));

        testTable.put("ccc", null);

        assertTrue(testTable.contains("ccc"));
    }

    @Test
    void testRemove() {
        testTable.put("aaa", "bbb");

        String previous = testTable.remove("aaa");

        assertEquals("bbb", previous);
        assertFalse(testTable.contains("aaa"));
    }

    @Test
    void testGet() {
        testTable.put("aaa", "bbb");

        assertEquals("bbb", testTable.get("aaa"));

        testTable.put("ccc", null);

        assertNull(testTable.get("ccc"));
    }

    @Test
    void testRePut() {
        testTable.put("aaa", "bbb");

        String previous = testTable.put("aaa", "ccc");

        assertEquals("bbb", previous);
        assertEquals("ccc", testTable.get("aaa"));
    }

    @Test
    void testRemoveNonexistent() {
        testTable.put("aaa", "bbb");

        String previous = testTable.remove("ccc");

        assertNull(previous);
    }

    @Test
    void testClear() {
        testTable.put("aaa", "bbb");

        testTable.clear();

        assertEquals(0, testTable.size());
    }

    @Test
    void testRehash() {
        for (int i = 0; i < 15; ++i) {
            testTable.put(i + "aaa", i + "bbb");
        }
        assertEquals(15, testTable.size());
        for (int i = 0; i < 15; ++i) {
            assertEquals(i + "bbb", testTable.get(i + "aaa"));
        }
        for (int i = 0; i < 15; ++i) {
            testTable.remove(i + "aaa");
        }
        assertEquals(0, testTable.size());
    }

    @Test
    void testNullKey() {
        testTable.put(null, "aaa");

        assertEquals("aaa", testTable.get(null));

        assertEquals("aaa", testTable.put(null, "bbb"));
        assertEquals("bbb", testTable.get(null));
        assertEquals("bbb", testTable.remove(null));
        assertEquals(0, testTable.size());
    }
}
