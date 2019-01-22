package com.example.hashtable;

class KeyValueList {
    class Link {
        final private String key;
        private String value;
        private Link next;

        private Link(String key, String value, Link next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        String getKey() {
            return key;
        }

        String getValue() {
            return value;
        }

        void setValue(String value) {
            this.value = value;
        }

        Link getNext() {
            return next;
        }
    }

    private Link head;


    Link find(String key) {
        Link current = getHead();
        while (current != null) {
            if (current.getKey().equals(key)) {
                return current;
            }
            current = current.getNext();
        }
        return null;
    }

    void remove(String key) {
        Link current = getHead();
        Link previous = null;
        while (current != null) {
            if (current.getKey().equals(key)) {
                if (previous == null) {
                    head = current.getNext();
                    return;
                }
                previous.next = current.getNext();
            }
            previous = current;
            current = current.getNext();
        }
    }

    void append(String key, String value) {
        head = new Link(key, value, head);
    }

    Link getHead() {
        return head;
    }
}
