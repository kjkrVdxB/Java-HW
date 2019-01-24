package com.example.hashtable;

import java.util.Objects;

class KeyValueList {
    private Link head;

    /**
     * Find a {@code Link} containing the {@code key}.
     * @return The found {@code Link}, or {@code null} if none contains the {@code key}.
     */
    public Link find(String key) {
        Link current = getHead();
        while (current != null) {
            if (Objects.equals(current.getKey(), key)) {
                return current;
            }
            current = current.getNext();
        }
        return null;
    }

    /**
     * Remove first {@code Link} containing the {@code key}, if there is any.
     */
    public void remove(String key) {
        Link current = getHead();
        Link previous = null;
        while (current != null) {
            if (Objects.equals(current.getKey(), key)) {
                if (previous == null) {
                    head = current.getNext();
                    return;
                }
                previous.next = current.getNext();
                return;
            }
            previous = current;
            current = current.getNext();
        }
    }

    /**
     * Append {@code key: value} pair to the list.
     */
    public void append(String key, String value) {
        head = new Link(key, value, head);
    }

    /**
     * Get the first {@code Link} in the list.
     */
    public Link getHead() {
        return head;
    }

    public class Link {
        final private String key;
        private String value;
        private Link next;

        private Link(String key, String value, Link next) {
            this.key = key;
            this.value = value;
            this.next = next;
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

        /**
         * Get next {@code Link} in the list.
         */
        public Link getNext() {
            return next;
        }
    }
}
