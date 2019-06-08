package com.example.unit;

import com.example.unit.api.*;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Simple unit test runner */
public class UnitTestRunner {
    private Class<?> unitTests;
    private List<Method> beforeClassMethods;
    private List<Method> afterClassMethods;
    private List<Method> beforeMethods;
    private List<Method> afterMethods;
    private List<Method> tests;
    private Constructor constructor;

    /**
     * Create unit test runner for the given test class. Validates some properties of the class,
     * for example that it has a no-parameters constructor, all (Before/After)Class methods are static,
     * all (Before/After/Test) methods are not static and there are no Test that is also Before or After.
     *
     * @param unitTests the class containing tests
     * @throws UnitTestRunnerException in case validation fails
     */
    public UnitTestRunner(@NonNull Class<?> unitTests) throws UnitTestRunnerException {
        Validate.notNull(unitTests);
        this.unitTests = unitTests;
        this.beforeClassMethods = getMethodsAnnotatedWith(BeforeClass.class);
        this.afterClassMethods = getMethodsAnnotatedWith(AfterClass.class);
        this.beforeMethods = getMethodsAnnotatedWith(Before.class);
        this.afterMethods = getMethodsAnnotatedWith(After.class);
        this.tests = getMethodsAnnotatedWith(Test.class);
        try {
            this.constructor = unitTests.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new UnitTestRunnerException("No constructor without parameters in test class");
        }
        validate();
    }

    /** Check the properties promised in constructor */
    private void validate() throws UnitTestRunnerException {
        for (var method: beforeClassMethods) {
            if ((method.getModifiers() & Modifier.STATIC) == 0) {
                throw new UnitTestRunnerException("BeforeClass methods must be static, but " + method.getName() + " is not");
            }
        }
        for (var method: afterClassMethods) {
            if ((method.getModifiers() & Modifier.STATIC) == 0) {
                throw new UnitTestRunnerException("AfterClass methods must be static, but " + method.getName() + " is not");
            }
        }
        for (var method: beforeMethods) {
            if ((method.getModifiers() & Modifier.STATIC) != 0) {
                throw new UnitTestRunnerException("Before methods must not be static, but " + method.getName() + " is");
            }
        }
        for (var method: afterMethods) {
            if ((method.getModifiers() & Modifier.STATIC) != 0) {
                throw new UnitTestRunnerException("After methods must not be static, but " + method.getName() + " is");
            }
        }
        for (var method: tests) {
            if ((method.getModifiers() & Modifier.STATIC) != 0) {
                throw new UnitTestRunnerException("Test methods must not be static, but " + method.getName() + " is");
            }
            if (method.getAnnotation(Before.class) != null || method.getAnnotation(After.class) != null) {
                throw new UnitTestRunnerException("Test methods must not be @After or @Before, but " + method.getName() + " is");
            }
        }
    }

    /**
     * Run the previously supplied test class.
     *
     * @return A list of test run results
     * @throws UnitTestRunnerException in case some of the calls fail with for example IllegalAccessException
     */
    public List<TestRunResult> runTests() throws UnitTestRunnerException {
        beforeClassMethods.forEach(beforeClass -> {
            try {
                beforeClass.invoke(null);
            } catch (Exception e) {
                throw new UnitTestRunnerException("Error invoking @BeforeClass method " + beforeClass.getName(), e);
            }
        });

        var testRunResults = new ArrayList<TestRunResult>();

        for (var test: tests) {
            testRunResults.add(runTest(test));
        }

        afterClassMethods.forEach(afterClass -> {
            try {
                afterClass.invoke(null);
            } catch (Exception e) {
                throw new UnitTestRunnerException("Error invoking @AfterClass method " + afterClass.getName(), e);
            }
        });

        return testRunResults;
    }

    /** Run one test denoted by the given method */
    private TestRunResult runTest(@NonNull Method test) throws UnitTestRunnerException {
        Validate.notNull(test);
        Object testInstanceCapture;
        try {
            testInstanceCapture = constructor.newInstance();
        } catch (Exception e) {
            throw new UnitTestRunnerException(e);
        }
        Object testInstance = testInstanceCapture;

        TestRunResult testRunResult = new TestRunResult(test.getName());

        String ignoreReason = test.getAnnotation(Test.class).ignore();

        if (!ignoreReason.equals("")) {
            testRunResult.setResult(TestResult.IGNORED, ignoreReason);
            return testRunResult;
        }

        beforeMethods.forEach(before -> {
            try {
                before.invoke(testInstance);
            } catch (Exception e) {
                throw new UnitTestRunnerException("Error invoking @Before method " + before.getName(), e);
            }
        });

        Class<? extends Exception> expected = test.getAnnotation(Test.class).expected();

        long startMillis = System.currentTimeMillis();
        try {
            test.invoke(testInstance);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (expected.isInstance(targetException)) {
                testRunResult.setResult(TestResult.SUCCESS, "Caught the expected exception");
            } else {
                if (expected.equals(NoExceptionExpected.class)) {
                    testRunResult.setResult(TestResult.FAILURE, "No exception expected, but " +
                                                                targetException.getClass().getName() +
                                                                " was thrown");
                } else {
                    testRunResult.setResult(TestResult.FAILURE, expected.getName() + " expected, but " +
                                                                targetException.getClass().getName() +
                                                                " was thrown");
                }
            }
        } catch (Exception e) {
            throw new UnitTestRunnerException("Error invoking @Test method " + test.getName(), e);
        }
        if (testRunResult.getResult() == TestResult.UNKNOWN && !expected.equals(NoExceptionExpected.class)) {
            testRunResult.setResult(TestResult.FAILURE, "The expected exception was not thrown");
        }
        if (testRunResult.getResult() == TestResult.UNKNOWN) {
            testRunResult.setResult(TestResult.SUCCESS, "Everything's fine");
        }
        testRunResult.setRunningTime(Duration.ofMillis(System.currentTimeMillis() - startMillis));

        afterMethods.forEach(after -> {
            try {
                after.invoke(testInstance);
            } catch (Exception e) {
                throw new UnitTestRunnerException("Error invoking @After method " + after.getName(), e);
            }
        });

        return testRunResult;
    }

    /** Utility method, filters out methods which have the given annotation */
    private List<Method> getMethodsAnnotatedWith(@NonNull Class<? extends Annotation> annotation) {
        Validate.notNull(annotation);
        return Arrays.stream(unitTests.getDeclaredMethods())
                .filter(method -> method.getAnnotation(annotation) != null)
                .collect(Collectors.toList());
    }

    /** A class representing a result of one test run */
    public static class TestRunResult {
        /** Typically equal to the name of the test function */
        @NonNull final String testName;
        /** Either the explanation of failure/success or the reason why the test was ignored */
        @NonNull String comment;
        @NonNull TestResult result = TestResult.UNKNOWN;
        @NonNull Duration runningTime = Duration.ZERO;

        private TestRunResult(@NonNull String testName) {
            Validate.notNull(testName);
            this.testName = testName;
        }

        public TestRunResult(@NonNull String testName, @NonNull TestResult result) {
            Validate.notNull(testName);
            Validate.notNull(result);
            this.testName = testName;
            setResult(result, "");
        }

        public TestResult getResult() {
            return result;
        }

        private void setResult(@NonNull TestResult result, @NonNull String comment) {
            Validate.notNull(result);
            Validate.notNull(comment);
            this.result = result;
            this.comment = comment;
        }

        public void setRunningTime(@NonNull Duration runningTime) {
            Validate.notNull(runningTime);
            this.runningTime = runningTime;
        }

        @NonNull
        public Duration getRunningTime() {
            return runningTime;
        }

        @NonNull
        public String getComment() {
            return comment;
        }

        @NonNull
        public String getTestName() {
            return testName;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TestRunResult)) {
                return false;
            }
            var other = (TestRunResult) obj;
            return testName.equals(other.testName) && result == other.result;
        }

        @Override
        public int hashCode() {
            return Objects.hash(testName, result);
        }

        @Override
        @NonNull
        public String toString() {
            return testName + " took " + runningTime.toMillis() + " ms, it's result is " + result.toString() +
                   (comment.equals("") ? "" : (" (" + comment + ")"));
        }
    }

    public enum TestResult {
        SUCCESS,
        FAILURE,
        IGNORED,
        UNKNOWN
    }
}
