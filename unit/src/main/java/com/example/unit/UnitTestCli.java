package com.example.unit;

import static java.lang.System.exit;

public class UnitTestCli {
    public static void main(String[] args) {
        if (args.length != 1) {
            printUsageAndExit();
        }
        Class<?> clazz;
        try {
            clazz = ClassLoader.getSystemClassLoader().loadClass(args[0]);
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
            return;
        }
        try {
            for (var result: new UnitTestRunner(clazz).runTests()) {
                System.out.println(result);
            }
        } catch (UnitTestRunnerException e) {
            System.out.println("Wrong test class format: " + e.getMessage());
        }
    }

    private static void printUsageAndExit() {
        System.out.println("One argument expected: the name of unit test class");
        exit(0);
    }
}
