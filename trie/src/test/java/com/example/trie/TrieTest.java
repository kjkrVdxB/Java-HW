package com.example.trie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrieTest {
    private Trie testTrie;

    @BeforeEach
    void init() {
        testTrie = new Trie();
    }

    @Test
    void testAddContains() {
        assertFalse(testTrie.contains(""));
        assertFalse(testTrie.add(""));
        assertTrue(testTrie.contains(""));

        assertFalse(testTrie.contains("aaa"));
        assertFalse(testTrie.add("aaa"));
        assertTrue(testTrie.contains("aaa"));
        assertFalse(testTrie.contains("aa"));
        assertFalse(testTrie.contains("aaaa"));

        assertFalse(testTrie.contains("aab"));
        assertFalse(testTrie.add("aab"));
        assertTrue(testTrie.contains("aab"));

        assertFalse(testTrie.contains("aa"));
        assertFalse(testTrie.add("aa"));
        assertTrue(testTrie.contains("aa"));

        assertThrows(IllegalArgumentException.class, () -> testTrie.add(null));
        assertThrows(IllegalArgumentException.class, () -> testTrie.contains(null));
    }

    @Test
    void testRemove() {
        testTrie.add("aaa");
        testTrie.add("aab");

        assertFalse(testTrie.remove("a"));
        assertFalse(testTrie.remove("aa"));
        assertTrue(testTrie.contains("aaa"));
        assertTrue(testTrie.remove("aaa"));
        assertFalse(testTrie.contains("aaa"));

        assertFalse(testTrie.remove("aaba"));
        assertTrue(testTrie.contains("aab"));

        testTrie.add("");
        testTrie.remove("");
        assertFalse(testTrie.contains(""));

        assertThrows(IllegalArgumentException.class, () -> testTrie.remove(null));
    }

    @Test
    void testSize() {
        assertEquals(0, testTrie.size());

        testTrie.add("aaa");
        assertEquals(1, testTrie.size());

        testTrie.add("aaa");
        assertEquals(1, testTrie.size());

        testTrie.add("aa");
        assertEquals(2, testTrie.size());

        testTrie.add("aab");
        assertEquals(3, testTrie.size());

        testTrie.add("");
        assertEquals(4, testTrie.size());

        testTrie.remove("aa");
        assertEquals(3, testTrie.size());

        testTrie.remove("aab");
        assertEquals(2, testTrie.size());

        testTrie.remove("");
        assertEquals(1, testTrie.size());

        testTrie.remove("aaa");
        assertEquals(0, testTrie.size());
    }

    @Test
    void testHowManyStartWithPrefix() {
        assertEquals(0, testTrie.howManyStartWithPrefix(""));

        testTrie.add("aaa");

        assertEquals(1, testTrie.howManyStartWithPrefix(""));
        assertEquals(1, testTrie.howManyStartWithPrefix("a"));
        assertEquals(1, testTrie.howManyStartWithPrefix("aa"));
        assertEquals(1, testTrie.howManyStartWithPrefix("aaa"));
        assertEquals(0, testTrie.howManyStartWithPrefix("aaaa"));
        assertEquals(0, testTrie.howManyStartWithPrefix("b"));

        testTrie.add("aab");

        assertEquals(2, testTrie.howManyStartWithPrefix(""));
        assertEquals(2, testTrie.howManyStartWithPrefix("aa"));

        testTrie.add("aca");

        assertEquals(3, testTrie.howManyStartWithPrefix(""));
        assertEquals(3, testTrie.howManyStartWithPrefix("a"));
        assertEquals(2, testTrie.howManyStartWithPrefix("aa"));

        testTrie.add("");

        assertEquals(4, testTrie.howManyStartWithPrefix(""));
        assertEquals(3, testTrie.howManyStartWithPrefix("a"));

        testTrie.add("aa");

        assertEquals(4, testTrie.howManyStartWithPrefix("a"));
        assertEquals(3, testTrie.howManyStartWithPrefix("aa"));
        assertEquals(1, testTrie.howManyStartWithPrefix("aaa"));
        assertEquals(1, testTrie.howManyStartWithPrefix("aab"));

        testTrie.remove("aaa");

        assertEquals(3, testTrie.howManyStartWithPrefix("a"));
        assertEquals(2, testTrie.howManyStartWithPrefix("aa"));
        assertEquals(0, testTrie.howManyStartWithPrefix("aaa"));
        assertEquals(1, testTrie.howManyStartWithPrefix("aab"));

        testTrie.remove("aa");

        assertEquals(1, testTrie.howManyStartWithPrefix("aa"));
        assertEquals(0, testTrie.howManyStartWithPrefix("aaa"));
        assertEquals(1, testTrie.howManyStartWithPrefix("aab"));

        assertThrows(IllegalArgumentException.class, () -> testTrie.howManyStartWithPrefix(null));
    }
}
