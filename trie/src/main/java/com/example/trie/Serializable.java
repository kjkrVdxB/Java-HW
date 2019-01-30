package com.example.trie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Interface for custom serialization. */
public interface Serializable {
    /** Serialize the object to {@code out}. */
    void serialize(OutputStream out) throws IOException;

    /** Replace current object with the object deserialized from {@code in}. */
    void deserialize(InputStream in) throws IOException;
}
