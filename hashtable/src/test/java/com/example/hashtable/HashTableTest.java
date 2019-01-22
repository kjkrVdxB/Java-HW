package com.example.hashtable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashTableTest {
    private static HashTable table;

    @BeforeEach
    void initTable() {
        table = new HashTable();
    }

    @Test
    void testSize() {
        assertEquals(0, table.size());

        table.put("aaa", "bbb");

        assertEquals(1, table.size());
    }

    @Test
    void testPutAndContains() {
        String previous = table.put("aaa", "bbb");

        assertNull(previous);
        assertTrue(table.contains("aaa"));
        assertFalse(table.contains("xxx"));

        table.put("ccc", null);

        assertTrue(table.contains("ccc"));
    }

    @Test
    void testRemove() {
        table.put("aaa", "bbb");

        String previous = table.remove("aaa");

        assertEquals("bbb", previous);
        assertFalse(table.contains("aaa"));
    }

    @Test
    void testGet() {
        table.put("aaa", "bbb");

        assertEquals("bbb", table.get("aaa"));

        table.put("ccc", null);

        assertNull(table.get("ccc"));
    }

    @Test
    void testReput() {
        table.put("aaa", "bbb");

        String previous = table.put("aaa", "ccc");

        assertEquals("bbb", previous);
        assertEquals("ccc", table.get("aaa"));
    }

    @Test
    void testRemoveNonexistent() {
        table.put("aaa", "bbb");

        String previous = table.remove("ccc");

        assertNull(previous);
    }

    @Test
    void testClear() {
        table.put("aaa", "bbb");

        table.clear();

        assertEquals(0, table.size());
    }

    @Test
    void testRehash() {
        for (int i = 0; i < 15; ++i) {
            table.put(Integer.valueOf(i).toString(), Integer.valueOf(i).toString() + "aaa");
        }
        assertEquals(15, table.size());
        for (int i = 0; i < 15; ++i) {
            assertEquals(Integer.valueOf(i).toString() + "aaa", table.get(Integer.valueOf(i).toString()));
        }
        for (int i = 0; i < 15; ++i) {
            table.remove(Integer.valueOf(i).toString());
        }
        assertEquals(0, table.size());
    }

    @Test
    void testNullKey() {
        assertThrows(NullPointerException.class, () -> { table.put(null, "aaa"); });
    }
}