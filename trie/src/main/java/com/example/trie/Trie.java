package com.example.trie;

import java.util.Hashtable;

public class Trie {
    private Node root;

    /**
     * Add {@code element} to trie.
     *
     * @return Whether the {@code element} was already in the trie.
     */
    public boolean add(String element) {
        Node current = root;
        for (char c : element.toCharArray()) {
            var next = current.children.get(c);
            if (next == null) {
                var newNode = new Node();

                // Note:
                // The trie can branch on the first half of a surrogate pair.
                // Despite being strange, that does not have any effect on the interface.
                //
                // (We allow inserting strings that end in a half of a surrogate pair.
                // But we don't check that the string is a valid unicode string anyway. The user
                // is responsible for that.)

                current.children.put(c, newNode);
                next = newNode;
            }
            current = next;
        }
        boolean wasBefore = current.isEnd;
        current.isEnd = true;
        if (!wasBefore) {
            for (char c : element.toCharArray()) {
                current.subtreeSize += 1;
                current = current.children.get(c);
            }
            current.subtreeSize += 1;
        }
        return wasBefore;
    }

    /**
     * Test if the trie contains the {@code element}.
     */
    public boolean contains(String element) {
        Node current = root;
        for (char c : element.toCharArray()) {
            var next = current.children.get(c);
            if (next == null) {
                return false;
            }
            current = next;
        }
        return current.isEnd;
    }

    /**
     * Remove {@code element} from trie.
     *
     * @return Whether the trie contained the {@code element}.
     */
    public boolean remove(String element) {
        if (!contains(element)) {
            return false;
        }
        Node current = root;
        current.subtreeSize -= 1;
        for (char c : element.toCharArray()) {
            var next = current.children.get(c);
            next.subtreeSize -= 1;
            if (next.subtreeSize == 0) {
                current.children.remove(c);
                return true;
            }
            current = next;
        }
        current.isEnd = false;
        return true;
    }

    /**
     * Return number of unique strings in the trie.
     */
    public int size() {
        return root.subtreeSize;
    }

    /**
     * Return number of unique string in the trie starting with the {@code prefix}.
     */
    public int howManyStartWithPrefix(String prefix) {
        Node current = root;
        for (char c : prefix.toCharArray()) {
            var next = current.children.get(c);
            if (next == null) {
                return 0;
            }
            current = next;
        }
        return current.subtreeSize;
    }

    private class Node {
        public Hashtable<Character, Node> children;
        public int subtreeSize;
        public boolean isEnd;

        public Node() {
            children = new Hashtable<>();
        }
    }
}
