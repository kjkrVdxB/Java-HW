package com.example.test3;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.stream.Collectors;

/** Class for computing MD5 hash of a directory in one thread */
public class SingleThreadHashing {
    public static byte[] hash(@NonNull Path root) {
        Validate.notNull(root);
        MessageDigest treeDigest;
        try {
            treeDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new DirectoryHashComputingException("MD5 algorithm not available", e);
        }
        if (Files.isDirectory(root)) {
            treeDigest.update(root.getFileName().toString().getBytes(Charset.forName("UTF-8")));
            try {
                for (var containingFile: Files.list(root)
                                              .filter(path -> Files.isRegularFile(path))
                                              .sorted(Comparator.comparing(Path::getFileName))
                                              .collect(Collectors.toList())) {
                    treeDigest.update(hash(containingFile));
                }
                return treeDigest.digest();
            } catch (IOException e) {
                throw new DirectoryHashComputingException("Error listing directory", e);
            }
        } else {
            return HashingUtils.getFileHash(root);
        }
    }
}
