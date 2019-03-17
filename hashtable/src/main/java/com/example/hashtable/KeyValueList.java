package com.example.hashtable;

import java.util.Objects;

class KeyValueList {
    private Link head;

    /**
     * Find an {@code Entry} containing the {@code key}.
     *
     * @return The found {@code Entry}, or {@code null} if none contains the {@code key}.
     */
    public Entry find(String key) {
        Link current = head;
        while (current != null) {
            if (Objects.equals(current.getElement().getKey(), key)) {
                return current.getElement();
            }
            current = current.getNext();
        }
        return null;
    }

    /** Remove first {@code Entry} containing the {@code key}, if there is any. */
    public void remove(String key) {
        Link current = head;
        Link previous = null;
        while (current != null) {
            if (Objects.equals(current.getElement().getKey(), key)) {
                if (previous == null) {
                    head = current.getNext();
                    return;
                }
                previous.setNext(current.getNext());
                return;
            }
            previous = current;
            current = current.getNext();
        }
    }

    /** Append {@code Entry} to the beginning of the list. */
    public void append(Entry element) {
        head = new Link(element, head);
    }

    /** Remove the first element in the list and return it. Return {@code null} if the list is empty; */
    public Entry popFront() {
        if (head == null) return null;
        var oldHead = head;
        head = head.next;
        return oldHead.element;
    }

    private static class Link {
        final private Entry element;
        private Link next;

        public Link(Entry element, Link next) {
            this.element = element;
            this.next = next;
        }

        public Entry getElement() {
            return element;
        }

        public Link getNext() {
            return next;
        }

        public void setNext(Link next) {
            this.next = next;
        }
    }

    public static class Entry {
        final private String key;
        private String value;

        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
