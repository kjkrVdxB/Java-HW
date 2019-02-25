package com.example.treeset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeSetTest {
    NavigableSet<String> testSet;

    @BeforeEach
    void init() {
        testSet = new TreeSet<>();
        testSet.add("2");
        testSet.add("6");
        testSet.add("0");
        testSet.add("4");
        testSet.add("8");
    }

    @Test
    void testAdd() {
        assertFalse(testSet.add("2"));
        assertFalse(testSet.add("8"));

        assertTrue(testSet.add("9"));
        assertFalse(testSet.add("9"));

        assertTrue(testSet.add("3"));
        assertFalse(testSet.add("3"));

        assertThrows(NullPointerException.class, () -> testSet.add(null));
    }

    @Test
    void testContains() {
        assertFalse(testSet.contains("9"));
        assertFalse(testSet.contains("7"));
        assertFalse(testSet.contains("5"));
        assertFalse(testSet.contains("3"));
        assertFalse(testSet.contains("1"));
        assertFalse(testSet.contains("("));
        assertTrue(testSet.contains("0"));
        assertTrue(testSet.contains("2"));
        assertTrue(testSet.contains("4"));
        assertTrue(testSet.contains("6"));
        assertTrue(testSet.contains("8"));

        testSet.add("3");
        testSet.add("7");

        assertTrue(testSet.contains("3"));
        assertTrue(testSet.contains("7"));

        assertThrows(NullPointerException.class, () -> testSet.contains(null));
    }


    @Test
    void testComparator() {
        var singletonTestSet = new TreeSet<String>((a, b) -> 0);
        assertTrue(singletonTestSet.add("1"));
        assertFalse(singletonTestSet.add("0"));
        assertTrue(singletonTestSet.contains("2"));
        assertNull(singletonTestSet.lower("2"));
        assertNull(singletonTestSet.higher("0"));
        assertEquals("1", singletonTestSet.floor("0"));
        assertEquals("1", singletonTestSet.ceiling("2"));

        var stringTestSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        stringTestSet.add("aB");
        assertTrue(stringTestSet.contains("ab"));
        assertTrue(stringTestSet.contains("AB"));
    }

    @Test
    void testRemove() {
        assertFalse(testSet.remove("7"));
        assertFalse(testSet.remove("1"));

        assertEquals(5, testSet.size());

        assertTrue(testSet.remove("2"));

        assertFalse(testSet.contains("2"));
        assertTrue(testSet.contains("4"));
        assertEquals(4, testSet.size());

        assertTrue(testSet.remove("0"));

        assertFalse(testSet.contains("0"));
        assertTrue(testSet.contains("4"));
        assertEquals(3, testSet.size());

        assertTrue(testSet.remove("8"));

        assertFalse(testSet.contains("8"));
        assertTrue(testSet.contains("4"));
        assertEquals(2, testSet.size());

        assertTrue(testSet.remove("6"));

        assertFalse(testSet.contains("6"));
        assertTrue(testSet.contains("4"));
        assertEquals(1, testSet.size());

        testSet.add("0");
        assertTrue(testSet.remove("4"));

        assertFalse(testSet.contains("4"));
        assertTrue(testSet.contains("0"));
        assertEquals(1, testSet.size());

        assertThrows(NullPointerException.class, () -> testSet.remove(null));
    }

    @Test
    void testSize() {
        assertEquals(5, testSet.size());

        testSet.add("10");

        assertEquals(6, testSet.size());

        testSet.remove("6");
        testSet.remove("2");

        assertEquals(4, testSet.size());
    }

    @Test
    void testLower() {
        assertEquals("6", testSet.lower("7"));
        assertEquals("6", testSet.lower("8"));
        assertNull(testSet.lower("0"));
        assertThrows(NullPointerException.class, () -> testSet.lower(null));
    }

    @Test
    void testFloor() {
        assertEquals("6", testSet.floor("6"));
        assertEquals("6", testSet.floor("7"));
        assertNull(testSet.floor("("));
        assertThrows(NullPointerException.class, () -> testSet.floor(null));
    }

    @Test
    void testCeiling() {
        assertEquals("6", testSet.ceiling("6"));
        assertEquals("6", testSet.ceiling("5"));
        assertNull(testSet.ceiling("9"));
        assertThrows(NullPointerException.class, () -> testSet.ceiling(null));
    }

    @Test
    void testHigher() {
        assertEquals("6", testSet.higher("4"));
        assertEquals("6", testSet.higher("5"));
        assertNull(testSet.higher("8"));
        assertThrows(NullPointerException.class, () -> testSet.higher(null));
    }

    @Test
    void testFirst() {
        assertEquals("0", testSet.first());

        var emptySet = new TreeSet<Integer>();
        assertThrows(NoSuchElementException.class, () -> emptySet.first());
    }

    @Test
    void testLast() {
        assertEquals("8", testSet.last());

        var emptySet = new TreeSet<Integer>();
        assertThrows(NoSuchElementException.class, () -> emptySet.last());
    }

    @Test
    void testDescendingSet() {
        var descendingTestSet = testSet.descendingSet();

        assertEquals("4", descendingTestSet.higher("6"));
        assertEquals("2", descendingTestSet.lower("0"));
        assertEquals("6", descendingTestSet.floor("6"));
        assertEquals("6", descendingTestSet.floor("5"));
        assertEquals("6", descendingTestSet.ceiling("6"));
        assertEquals("4", descendingTestSet.ceiling("5"));
        assertNull(descendingTestSet.lower("8"));

        assertEquals(5, descendingTestSet.size());

        testSet.add("5");
        assertEquals(6, descendingTestSet.size());
        assertTrue(descendingTestSet.contains("5"));

        descendingTestSet.add("3");
        descendingTestSet.add("a");

        assertEquals(8, testSet.size());
        assertTrue(testSet.contains("3"));
        assertTrue(testSet.contains("a"));

        assertEquals("a", descendingTestSet.first());
        assertEquals("0", descendingTestSet.last());
    }

    @Test
    void testIterator() {
        testSet.add("(");
        testSet.add("1");
        String[] expectedContent = {"(", "0", "1", "2", "4", "6", "8"};
        var it = testSet.iterator();
        for (String a : expectedContent) {
            if (a.equals("(")) {
                assertThrows(IllegalStateException.class, () -> it.remove());
            }

            assertTrue(it.hasNext());
            assertEquals(a, it.next());

            if (a.equals("2") || a.equals("0")) {
                it.remove();
            }

            if (a.equals("2")) {
                assertThrows(IllegalStateException.class, () -> it.remove());
            }
        }
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, () -> it.next());

        assertEquals(5, testSet.size());
        assertFalse(testSet.contains("2"));
        assertFalse(testSet.contains("0"));

        testSet = new TreeSet<>();
        assertFalse(testSet.iterator().hasNext());
    }

    @Test
    void testDescendingIterator() {
        testSet.add("(");
        testSet.add("1");
        String[] expectedContent = {"8", "6", "4", "2", "1", "0", "("};
        var it = testSet.descendingIterator();
        for (String a : expectedContent) {
            if (a.equals("8")) {
                assertThrows(IllegalStateException.class, () -> it.remove());
            }

            assertTrue(it.hasNext());
            assertEquals(a, it.next());

            if (a.equals("2") || a.equals("0")) {
                it.remove();
            }

            if (a.equals("2")) {
                assertThrows(IllegalStateException.class, () -> it.remove());
            }
        }
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, () -> it.next());

        assertEquals(5, testSet.size());
        assertFalse(testSet.contains("2"));
        assertFalse(testSet.contains("0"));

        testSet = new TreeSet<>();
        assertFalse(testSet.descendingIterator().hasNext());
    }

    @Test
    void testConcurrentIteratorAccess() {
        var it1 = testSet.iterator();
        var it2 = testSet.iterator();

        testSet.add("1");

        assertThrows(ConcurrentModificationException.class, () -> it1.next());
        assertThrows(ConcurrentModificationException.class, () -> {
            if (it2.hasNext()) {
                it2.next();
            }
        });

        var it3 = testSet.descendingIterator();

        testSet.remove("3");

        assertTrue(it3.hasNext());
        it3.next();

        testSet.remove("2");

        assertThrows(ConcurrentModificationException.class, () -> it3.next());
    }
}