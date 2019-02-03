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
    protected NavigableSet<Integer> testSet;

    @BeforeEach
    void init() {
        testSet = new TreeSet<>();
        testSet.add(2);
        testSet.add(6);
        testSet.add(0);
        testSet.add(4);
        testSet.add(8);
    }

    @Test
    void testAddContains() {
        assertFalse(testSet.contains(9));
        assertFalse(testSet.contains(7));
        assertFalse(testSet.contains(5));
        assertFalse(testSet.contains(3));
        assertFalse(testSet.contains(1));
        assertFalse(testSet.contains(-1));
        assertTrue(testSet.contains(0));
        assertTrue(testSet.contains(2));
        assertTrue(testSet.contains(4));
        assertTrue(testSet.contains(6));
        assertTrue(testSet.contains(8));

        assertFalse(testSet.add(2));
        assertFalse(testSet.add(8));

        assertTrue(testSet.add(10));
        assertTrue(testSet.add(3));

        assertThrows(IllegalArgumentException.class, () -> testSet.add(null));
        assertThrows(IllegalArgumentException.class, () -> testSet.contains(null));
    }


    @Test
    void testComparator() {
        var singletonTestSet = new TreeSet<Integer>((a, b) -> 0);
        assertTrue(singletonTestSet.add(1));
        assertFalse(singletonTestSet.add(0));
        assertTrue(singletonTestSet.contains(2));
        assertNull(singletonTestSet.lower(2));
        assertNull(singletonTestSet.higher(0));
        assertEquals(1, (int) singletonTestSet.floor(0));
        assertEquals(1, (int) singletonTestSet.ceiling(2));

        var stringTestSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        stringTestSet.add("aB");
        assertTrue(stringTestSet.contains("ab"));
        assertTrue(stringTestSet.contains("AB"));
    }

    @Test
    void testRemove() {
        assertFalse(testSet.remove(7));
        assertFalse(testSet.remove(1));

        assertEquals(5, testSet.size());

        assertTrue(testSet.remove(2));

        assertFalse(testSet.contains(2));
        assertTrue(testSet.contains(4));
        assertEquals(4, testSet.size());

        assertTrue(testSet.remove(0));

        assertFalse(testSet.contains(0));
        assertTrue(testSet.contains(4));
        assertEquals(3, testSet.size());

        assertTrue(testSet.remove(8));

        assertFalse(testSet.contains(8));
        assertTrue(testSet.contains(4));
        assertEquals(2, testSet.size());

        assertTrue(testSet.remove(6));

        assertFalse(testSet.contains(6));
        assertTrue(testSet.contains(4));
        assertEquals(1, testSet.size());

        testSet.add(0);
        assertTrue(testSet.remove(4));

        assertFalse(testSet.contains(4));
        assertTrue(testSet.contains(0));
        assertEquals(1, testSet.size());

        assertThrows(IllegalArgumentException.class, () -> testSet.remove(null));
    }

    @Test
    void testSize() {
        assertEquals(5, testSet.size());

        testSet.add(10);

        assertEquals(6, testSet.size());

        testSet.remove(6);
        testSet.remove(2);

        assertEquals(4, testSet.size());
    }

    @Test
    void testFirstLast() {
        assertEquals(0, (int) testSet.first());
        assertEquals(8, (int) testSet.last());

        var emptySet = new TreeSet<Integer>();
        assertThrows(NoSuchElementException.class, () -> emptySet.first());
        assertThrows(NoSuchElementException.class, () -> emptySet.last());
    }

    @Test
    void testDescendingSet() {
        var descendingTestSet = testSet.descendingSet();

        assertEquals(4, (int) descendingTestSet.higher(6));
        assertEquals(2, (int) descendingTestSet.lower(0));
        assertEquals(6, (int) descendingTestSet.floor(6));
        assertEquals(6, (int) descendingTestSet.floor(5));
        assertEquals(6, (int) descendingTestSet.ceiling(6));
        assertEquals(4, (int) descendingTestSet.ceiling(5));

        assertEquals(5, descendingTestSet.size());

        testSet.add(5);
        assertEquals(6, descendingTestSet.size());
        assertTrue(descendingTestSet.contains(5));

        descendingTestSet.add(3);
        descendingTestSet.add(11);

        assertEquals(8, testSet.size());
        assertTrue(testSet.contains(3));
        assertTrue(testSet.contains(11));

        assertEquals(11, (int) descendingTestSet.first());
        assertEquals(0, (int) descendingTestSet.last());
    }

    @Test
    void testIterator() {
        testSet.add(-1);
        testSet.add(1);
        int[] expectedContent = {-1, 0, 1, 2, 4, 6, 8};
        var it = testSet.iterator();
        for (int a : expectedContent) {
            if (a == -1) {
                assertThrows(IllegalStateException.class, () -> it.remove());
            }

            assertTrue(it.hasNext());
            assertEquals(a, (int) it.next());

            if (a == 2 || a == 0) {
                it.remove();
            }

            if (a == 2) {
                assertThrows(IllegalStateException.class, () -> it.remove());
            }
        }
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, () -> it.next());

        assertEquals(5, testSet.size());
        assertFalse(testSet.contains(2));
        assertFalse(testSet.contains(0));

        testSet = new TreeSet<>();
        assertFalse(testSet.iterator().hasNext());
    }

    @Test
    void testDescendingIterator() {
        testSet.add(-1);
        testSet.add(1);
        int[] expectedContent = {8, 6, 4, 2, 1, 0, -1};
        var it = testSet.descendingIterator();
        for (int a : expectedContent) {
            if (a == 8) {
                assertThrows(IllegalStateException.class, () -> it.remove());
            }

            assertTrue(it.hasNext());
            assertEquals(a, (int) it.next());

            if (a == 2 || a == 0) {
                it.remove();
            }

            if (a == 2) {
                assertThrows(IllegalStateException.class, () -> it.remove());
            }

        }
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, () -> it.next());

        assertEquals(5, testSet.size());
        assertFalse(testSet.contains(2));
        assertFalse(testSet.contains(0));

        testSet = new TreeSet<>();
        assertFalse(testSet.descendingIterator().hasNext());
    }

    @Test
    void testConcurrentIteratorAccess() {
        var it1 = testSet.iterator();
        var it2 = testSet.iterator();

        testSet.add(1);

        assertThrows(ConcurrentModificationException.class, () -> it1.next());
        assertThrows(ConcurrentModificationException.class, () -> {
            if (it2.hasNext()) {
                it2.next();
            }
        });

        var it3 = testSet.descendingIterator();

        testSet.remove(3);

        assertTrue(it3.hasNext());
        it3.next();

        testSet.remove(2);

        assertThrows(ConcurrentModificationException.class, () -> it3.next());
    }
}