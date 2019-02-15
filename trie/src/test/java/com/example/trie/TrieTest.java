package com.example.trie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    @Test
    void testSerialize() throws IOException {
        testTrie.add("ca");
        testTrie.add("aab");
        testTrie.add("");
        testTrie.add("aaa");
        testTrie.add("c");

        var serializedTrieOutput = new ByteArrayOutputStream(100);

        testTrie.serialize(serializedTrieOutput);

        assertEquals(47, serializedTrieOutput.size());

        var serializedTrieInput = new DataInputStream(new ByteArrayInputStream(serializedTrieOutput.toByteArray()));

        assertTrue(serializedTrieInput.readBoolean());
        assertEquals(2, serializedTrieInput.readInt());
        assertEquals('a', serializedTrieInput.readChar());
        assertFalse(serializedTrieInput.readBoolean());
        assertEquals(1, serializedTrieInput.readInt());
        assertEquals('a', serializedTrieInput.readChar());
        assertFalse(serializedTrieInput.readBoolean());
        assertEquals(2, serializedTrieInput.readInt());
        assertEquals('a', serializedTrieInput.readChar());
        assertTrue(serializedTrieInput.readBoolean());
        assertEquals(0, serializedTrieInput.readInt());
        assertEquals('b', serializedTrieInput.readChar());
        assertTrue(serializedTrieInput.readBoolean());
        assertEquals(0, serializedTrieInput.readInt());
        assertEquals('c', serializedTrieInput.readChar());
        assertTrue(serializedTrieInput.readBoolean());
        assertEquals(1, serializedTrieInput.readInt());
        assertEquals('a', serializedTrieInput.readChar());
        assertTrue(serializedTrieInput.readBoolean());
        assertEquals(0, serializedTrieInput.readInt());
        assertEquals(-1, serializedTrieInput.read());

        assertThrows(IllegalArgumentException.class, () -> this.testTrie.serialize(null));
    }

    @Test
    void testDeserialize() throws IOException {
        var dummyByteOutput = new ByteArrayOutputStream(100);
        var serializedTrieOutput = new DataOutputStream(dummyByteOutput);

        serializedTrieOutput.writeBoolean(true);
        serializedTrieOutput.writeInt(2);
        serializedTrieOutput.writeChar('a');
        serializedTrieOutput.writeBoolean(false);
        serializedTrieOutput.writeInt(1);
        serializedTrieOutput.writeChar('a');
        serializedTrieOutput.writeBoolean(false);
        serializedTrieOutput.writeInt(2);
        serializedTrieOutput.writeChar('a');
        serializedTrieOutput.writeBoolean(true);
        serializedTrieOutput.writeInt(0);
        serializedTrieOutput.writeChar('b');
        serializedTrieOutput.writeBoolean(true);
        serializedTrieOutput.writeInt(0);
        serializedTrieOutput.writeChar('c');
        serializedTrieOutput.writeBoolean(true);
        serializedTrieOutput.writeInt(1);
        serializedTrieOutput.writeChar('a');
        serializedTrieOutput.writeBoolean(true);
        serializedTrieOutput.writeInt(0);

        assertEquals(47, serializedTrieOutput.size());

        var dummyByteInput = new ByteArrayInputStream(dummyByteOutput.toByteArray());

        testTrie.deserialize(dummyByteInput);

        assertEquals(-1, dummyByteInput.read());

        assertEquals(5, testTrie.size());
        assertTrue(testTrie.contains("aaa"));
        assertTrue(testTrie.contains("aab"));
        assertTrue(testTrie.contains("ca"));
        assertTrue(testTrie.contains("c"));
        assertTrue(testTrie.contains(""));
        assertEquals(2, testTrie.howManyStartWithPrefix("a"));
        assertEquals(2, testTrie.howManyStartWithPrefix("c"));
        assertEquals(1, testTrie.howManyStartWithPrefix("ca"));

        assertThrows(IllegalArgumentException.class, () -> this.testTrie.deserialize(null));
    }
}
