package com.example.test3;

import org.apache.commons.codec.binary.Hex;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Supplier;

import static java.lang.System.*;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            out.println("One path parameter expected");
        }
        var rootPath = Paths.get(args[0]);
        printHashComputationStatistics(() -> ForkJoinHashing.hash(rootPath), "ForkJoinHashing");
        printHashComputationStatistics(() -> SingleThreadHashing.hash(rootPath), "SingleThreadHashing");
    }

    private static void printHashComputationStatistics(@NonNull Supplier<byte[]> supplier, @NonNull String hasherType) {
        out.print("Running hashing using " + hasherType + "... ");
        out.flush();
        long startMillis = currentTimeMillis();
        byte[] result;
        try {
            result = supplier.get();
        } catch (DirectoryHashComputingException e) {
            out.println(e.getMessage());
            return;
        }
        long runningTime = currentTimeMillis() - startMillis;
        out.println("finished");
        out.print("It took ");
        out.print(runningTime / 1000);
        out.println(" ms");
        out.print("The resulting hash is ");
        out.println(Hex.encodeHexString(result));
    }
}
