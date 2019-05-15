package com.example.test3;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/** Class for computing MD5 hash of a directory using ForkJoinPool */
public class ForkJoinHashing {

    public static byte[] hash(@NonNull Path root) {
        Validate.notNull(root);
        var rootTask = new HashingTask(root);
        var pool = new ForkJoinPool();
        pool.execute(rootTask);
        return rootTask.compute();
    }

    private static class HashingTask extends RecursiveTask<byte[]> {
        private @NonNull Path root;

        private HashingTask(@NonNull Path root) {
            Validate.notNull(root);
            this.root = root;
        }

        @Override
        protected byte[] compute() {
            if (Files.isDirectory(root)) {
                MessageDigest treeDigest = null;
                try {
                    treeDigest = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    throw new DirectoryHashComputingException("MD5 algorithm not available", e);
                }
                try {
                    var tasks = new ArrayList<HashingTask>();
                    for (var containingFile: Files.list(root).filter(path -> Files.isRegularFile(path)).sorted(Comparator.comparing(Path::getFileName)).collect(Collectors
                                                                                                                                                                        .toList())) {
                        var newTask = new HashingTask(containingFile);
                        newTask.fork();
                        tasks.add(newTask);
                    }
                    for (var task: tasks) {
                        treeDigest.update(task.join());
                    }
                } catch (IOException e) {
                    throw new DirectoryHashComputingException("Error listing directory", e);
                }
                return treeDigest.digest();
            } else {
                return HashingUtils.getFileHash(root);
            }
        }
    }
}
