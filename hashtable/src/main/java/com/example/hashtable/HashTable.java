package com.example.hashtable;

import static java.lang.StrictMath.max;

/** List-based hash table. */
public class HashTable {
    /** Invariant: buckets.length is always at least MINIMUM_BUCKETS */
    final private static int MINIMUM_BUCKETS = 10;

    /** In put(String), rehash is happening when {@code size / buckets.length >= 1 / INVERSE_PUT_REHASH_THRESHOLD}. */
    final private static int INVERSE_PUT_REHASH_THRESHOLD = 2;

    /**
     * In put(String), the target number of buckets after a rehash is
     * {@code size * PUT_REHASH_RELATIVE_BUCKETS_NUMBER}.
     */
    final private static int PUT_REHASH_RELATIVE_BUCKETS_NUMBER = 4;

    /**
     * In remove(String), rehash is happening when
     * {@code size / buckets.length <= 1 / INVERSE_REMOVE_REHASH_THRESHOLD}.
     */
    final private static int INVERSE_REMOVE_REHASH_THRESHOLD = 8;

    /**
     * In remove(String), the target number of buckets after a rehash is
     * {@code size * REMOVE_REHASH_RELATIVE_BUCKETS_NUMBER}.
     */
    final private static int REMOVE_REHASH_RELATIVE_BUCKETS_NUMBER = 4;
    private int size;
    private KeyValueList[] buckets;

    /** New table with a predefined number of buckets. */
    public HashTable() {
        size = 0;
        buckets = new KeyValueList[MINIMUM_BUCKETS];
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

    /** Rehash using at least {@code newNumBuckets} buckets. */
    private void rehash(int newNumBuckets) {
        newNumBuckets = max(newNumBuckets, MINIMUM_BUCKETS);
        int oldNumBuckets = buckets.length;
        KeyValueList[] newBuckets = new KeyValueList[newNumBuckets];
        for (int i = 0; i < newBuckets.length; ++i) {
            newBuckets[i] = new KeyValueList();
        }
        KeyValueList[] oldBuckets = buckets;
        buckets = newBuckets;

        for (int i = 0; i < oldNumBuckets; ++i) {
            KeyValueList.Entry current;
            while ((current = oldBuckets[i].popFront()) != null) {
                bucket(current.getKey()).append(current);
            }
        }
    }

    /** Returns number of elements in the table. */
    public int size() {
        return size;
    }

    /** Check whether there is a pair in the table with specified {@code key}. */
    public boolean contains(String key) {
        return bucket(key).find(key) != null;
    }

    /** Get the {@code String} associated with the {@code key}, or {@code null} if there is none. */
    public String get(String key) {
        KeyValueList.Entry foundPosition = bucket(key).find(key);
        if (foundPosition == null) return null;
        return foundPosition.getValue();
    }

    /**
     * Associate {@code value} with {@code key}. Note that {@code null} keys are supported, while {@code null}
     * values are not.
     *
     * @return Previous value associated with the {@code key}.
     * @throws IllegalArgumentException if the {@code value} is {@code null}.
     */
    public String put(String key, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null value is not supported");
        }
        KeyValueList targetBucket = bucket(key);
        KeyValueList.Entry foundPosition = targetBucket.find(key);
        if (foundPosition == null) {
            targetBucket.append(new KeyValueList.Entry(key, value));
            ++size;
            if (size * INVERSE_PUT_REHASH_THRESHOLD >= buckets.length) {
                rehash(size * PUT_REHASH_RELATIVE_BUCKETS_NUMBER);
            }
            return null;
        }
        String oldValue = foundPosition.getValue();
        foundPosition.setValue(value);
        return oldValue;

    }

    /**
     * Remove the pair associated with {@code key}. Memory used by buckets is reclaimed.
     *
     * @return Value associated with {@code key} before removal.
     */
    public String remove(String key) {
        KeyValueList targetBucket = bucket(key);
        KeyValueList.Entry foundPosition = targetBucket.find(key);
        if (foundPosition == null) {
            return null;
        }
        String oldValue = foundPosition.getValue();
        targetBucket.remove(key);
        --size;
        if (size * INVERSE_REMOVE_REHASH_THRESHOLD <= buckets.length) {
            rehash(size * REMOVE_REHASH_RELATIVE_BUCKETS_NUMBER);
        }
        return oldValue;
    }

    /** Remove all elements. Memory used by buckets is reclaimed. */
    public void clear() {
        size = 0;
        buckets = new KeyValueList[MINIMUM_BUCKETS];
        for (int i = 0; i < buckets.length; ++i) {
            buckets[i] = new KeyValueList();
        }
    }
}
