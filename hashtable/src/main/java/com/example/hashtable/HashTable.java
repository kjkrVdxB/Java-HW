package com.example.hashtable;

/**
 * List based hash table.
 */
public class HashTable {
    private static int BUCKETS_GROWTH_RATE = 2;
    private static int INITIAL_BUCKETS = 10;
    private static int REHASH_TRESHOLD = 2;
    private int size;
    private KeyValueList[] buckets;

    /**
     * New table with a predefined number of buckets.
     */
    public HashTable() {
        this(INITIAL_BUCKETS);
    }

    /**
     * New table with {@code numBuckets} buckets.
     */
    public HashTable(int numBuckets) {
        size = 0;
        buckets = new KeyValueList[numBuckets];
        for (int i = 0; i < buckets.length; ++i) {
            buckets[i] = new KeyValueList();
        }
    }

    private int keyToIndex(String key) {
        // buckets.length is always at least 1
        if (key == null) return 0;
        return Math.floorMod(key.hashCode(), buckets.length);
    }

    private KeyValueList bucket(String key) {
        return buckets[keyToIndex(key)];
    }

    private void rehash() {
        int oldNumBuckets = buckets.length;
        KeyValueList[] newBuckets = new KeyValueList[buckets.length * BUCKETS_GROWTH_RATE];
        for (int i = 0; i < newBuckets.length; ++i) {
            newBuckets[i] = new KeyValueList();
        }
        KeyValueList[] oldBuckets = buckets;
        buckets = newBuckets;

        for (int i = 0; i < oldNumBuckets; ++i) {
            KeyValueList.Link current = oldBuckets[i].getHead();
            while (current != null) {
                bucket(current.getKey()).append(current.getKey(), current.getValue());
                current = current.getNext();
            }
        }
    }

    /**
     * @return Number of elements in the table.
     */
    public int size() {
        return size;
    }

    /**
     * Check whether there is a pair in the table with specified {@code key}.
     */
    public boolean contains(String key) {
        return bucket(key).find(key) != null;
    }

    /**
     * Get the {@code String} associated with the {@code key}, or {@code null} if there is none. Note that {@code null}
     * is also returned if the associated {@code String} is {@code null}. Use {@code contains} to resolve the ambiguity.
     */
    public String get(String key) {
        KeyValueList.Link foundPosition = bucket(key).find(key);
        if (foundPosition == null) return null;
        return foundPosition.getValue();
    }

    /**
     * Associate {@code value} with {@code key}. Note that {@code null} keys are not supported, while {@code null}
     * values are.
     *
     * @return Previous value associated with the {@code key}. Note that {@code null} is also returned if the previous
     * associated {@code String} was {@code null}. Use {@code contains} to resolve the ambiguity.
     */
    public String put(String key, String value) {
        KeyValueList targetBucket = bucket(key);
        KeyValueList.Link foundPosition = targetBucket.find(key);
        if (foundPosition == null) {
            targetBucket.append(key, value);
            ++size;
            if (size * REHASH_TRESHOLD >= buckets.length) {
                rehash();
            }
            return null;
        }
        String oldValue = foundPosition.getValue();
        foundPosition.setValue(value);
        return oldValue;

    }

    /**
     * Remove the pair associated with {@code key}. Note that the number of buckets will not decrease.
     *
     * @return Value associated with {@code key} before removal. Note that {@code null} is also returned if the previous
     * associated {@code String} was {@code null}. Use {@code contains} to resolve the ambiguity.
     */
    public String remove(String key) {
        KeyValueList targetBucket = bucket(key);
        KeyValueList.Link foundPosition = targetBucket.find(key);
        if (foundPosition == null) {
            return null;
        }
        String oldValue = foundPosition.getValue();
        targetBucket.remove(key);
        --size;
        return oldValue;
    }

    /**
     * Remove all elements. Note that the number of buckets will not decrease.
     */
    public void clear() {
        size = 0;
        for (int i = 0; i < buckets.length; ++i) {
            buckets[i] = new KeyValueList();
        }
    }
}
