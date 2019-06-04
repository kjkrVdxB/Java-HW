package com.example.test3;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Routines common for hashing algorithms */
public class HashingUtils {

    public static byte @NonNull [] getFileHash(@NonNull Path filePath) {
        MessageDigest fileDigest;
        try {
            fileDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new DirectoryHashComputingException("MD5 algorithm not available", e);
        }
        File file = filePath.toFile();
        try (var inputStream = new FileInputStream(file);
             var digestStream = new DigestInputStream(inputStream, fileDigest)) {
            while (digestStream.read() != -1) {
            }
        } catch (IOException e) {
            throw new DirectoryHashComputingException("Error reading file", e);
        }
        return fileDigest.digest();
    }
}
