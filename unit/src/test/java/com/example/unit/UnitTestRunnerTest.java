package com.example.unit;

import com.example.unit.UnitTestRunner.TestResult;
import com.example.unit.UnitTestRunner.TestRunResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class UnitTestRunnerTest {
    static class Test1 {
        @com.example.unit.api.Test
        void test1() {
        }
    }

    @Test
    void testSimple() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.SUCCESS)};

        assertResultListEquals(expectedResult, new UnitTestRunner(Test1.class).runTests());
    }

    static class Test2 {
        static int beforeCounter = 0;

        @com.example.unit.api.Before
        void before() {
            ++beforeCounter;
        }

        @com.example.unit.api.Test
        void test1() {
        }

        @com.example.unit.api.Test
        void test2() {
        }
    }

    @Test
    void testBeforeAreCalled() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.SUCCESS),
                                                 new TestRunResult("test2", TestResult.SUCCESS)};

        assertResultListEquals(expectedResult, new UnitTestRunner(Test2.class).runTests());
        assertEquals(2, Test2.beforeCounter);
    }

    static class Test3 {
        static int afterCounter = 0;

        @com.example.unit.api.Before
        void after() {
            ++afterCounter;
        }

        @com.example.unit.api.Test
        void test1() {
        }

        @com.example.unit.api.Test
        void test2() {
        }
    }

    @Test
    void testAfterAreCalled() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.SUCCESS),
                                                 new TestRunResult("test2", TestResult.SUCCESS)};

        assertResultListEquals(expectedResult, new UnitTestRunner(Test3.class).runTests());
        assertEquals(2, Test3.afterCounter);
    }

    static class Test4 {
        static int beforeCounter = 0;

        @com.example.unit.api.Before
        void before() {
            ++beforeCounter;
        }

        @com.example.unit.api.Test
        void test1() {
            if (beforeCounter != 1) {
                throw new IllegalStateException();
            }
        }
    }

    @Test
    void testBeforeAreCalledBefore() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.SUCCESS)};

        assertResultListEquals(expectedResult, new UnitTestRunner(Test4.class).runTests());
        assertEquals(1, Test4.beforeCounter);
    }

    static class Test5 {
        static int afterCounter = 0;

        @com.example.unit.api.After
        void after() {
            ++afterCounter;
        }

        @com.example.unit.api.Test
        void test1() {
            if (afterCounter != 0) {
                throw new IllegalStateException();
            }
        }
    }

    @Test
    void testAfterAreCalledAfter() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.SUCCESS)};

        assertResultListEquals(expectedResult, new UnitTestRunner(Test5.class).runTests());
        assertEquals(1, Test5.afterCounter);
    }

    static class Test6 {
        static int beforeClassCounter = 0;

        @com.example.unit.api.BeforeClass
        static void beforeClass() {
            ++beforeClassCounter;
        }

        @com.example.unit.api.Test
        void test1() {
        }

        @com.example.unit.api.Test
        void test2() {
        }
    }

    @Test
    void testBeforeClassIsCalledOnce() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.SUCCESS),
                                                 new TestRunResult("test2", TestResult.SUCCESS)};

        assertResultListEquals(expectedResult, new UnitTestRunner(Test6.class).runTests());
        assertEquals(1, Test6.beforeClassCounter);
    }

    static class Test7 {
        static int afterClassCounter = 0;

        @com.example.unit.api.AfterClass
        static void afterClass() {
            ++afterClassCounter;
        }

        @com.example.unit.api.Test
        void test1() {
        }

        @com.example.unit.api.Test
        void test2() {
        }
    }

    @Test
    void testAfterClassIsCalledOnce() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.SUCCESS),
                                                 new TestRunResult("test2", TestResult.SUCCESS)};

        assertResultListEquals(expectedResult, new UnitTestRunner(Test7.class).runTests());
        assertEquals(1, Test7.afterClassCounter);
    }

    static class Test8 {
        static int beforeClassCounter = 0;

        @com.example.unit.api.BeforeClass
        static void beforeClass() {
            ++beforeClassCounter;
        }

        @com.example.unit.api.Test
        void test1() {
            if (beforeClassCounter != 1) {
                throw new IllegalStateException();
            }
        }

        @com.example.unit.api.Test
        void test2() {
            if (beforeClassCounter != 1) {
                throw new IllegalStateException();
            }
        }
    }

    @Test
    void testBeforeClassIsCalledBefore() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.SUCCESS),
                                                 new TestRunResult("test2", TestResult.SUCCESS)};

        assertResultListEquals(expectedResult, new UnitTestRunner(Test8.class).runTests());
        assertEquals(1, Test8.beforeClassCounter);
    }

    static class Test9 {
        static int afterClassCounter = 0;

        @com.example.unit.api.AfterClass
        static void afterClass() {
            ++afterClassCounter;
        }

        @com.example.unit.api.Test
        void test1() {
            if (afterClassCounter != 0) {
                throw new IllegalStateException();
            }
        }

        @com.example.unit.api.Test
        void test2() {
            if (afterClassCounter != 0) {
                throw new IllegalStateException();
            }
        }
    }

    @Test
    void testAfterClassIsCalledAfter() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.SUCCESS),
                                                 new TestRunResult("test2", TestResult.SUCCESS)};

        assertResultListEquals(expectedResult, new UnitTestRunner(Test9.class).runTests());
        assertEquals(1, Test9.afterClassCounter);
    }

    static class Test10 {
        @com.example.unit.api.Test
        void test1() {
            throw new IllegalCallerException();
        }
    }

    @Test
    void testSimpleFail() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.FAILURE)};

        assertResultListEquals(expectedResult, new UnitTestRunner(Test10.class).runTests());
    }

    static class Test11 {
        @com.example.unit.api.Test(ignore = "because")
        void test1() {
            throw new IllegalCallerException();
        }
    }

    @Test
    void testSimpleIgnored() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.IGNORED)};

        var result = new UnitTestRunner(Test11.class).runTests();
        assertResultListEquals(expectedResult, result);
        assertEquals("because", result.get(0).getComment());
    }

    static class Test12 {
        @com.example.unit.api.Test(expected = IllegalStateException.class)
        void test1() {
            throw new IllegalStateException();
        }
    }

    @Test
    void testSimpleExpectedException() {
        var expectedResult = new TestRunResult[]{new TestRunResult("test1", TestResult.SUCCESS)};

        assertResultListEquals(expectedResult, new UnitTestRunner(Test12.class).runTests());
    }

    static class Test13 {
        @com.example.unit.api.Test
        @com.example.unit.api.Before
        void test1() {
            throw new IllegalStateException();
        }
    }

    @Test
    void testBeforeAndTestProhibited() {
          assertThrows(UnitTestRunnerException.class, () -> new UnitTestRunner(Test13.class));
    }

    static class Test14 {
        @com.example.unit.api.Test
        @com.example.unit.api.After
        void test1() {
            throw new IllegalStateException();
        }
    }

    @Test
    void testAfterAndTestProhibited() {
        assertThrows(UnitTestRunnerException.class, () -> new UnitTestRunner(Test14.class));
    }

    static class Test15 {
        @com.example.unit.api.BeforeClass
        void test1() {
            throw new IllegalStateException();
        }
    }

    @Test
    void testNonStaticBeforeClassProhibited() {
        assertThrows(UnitTestRunnerException.class, () -> new UnitTestRunner(Test15.class));
    }

    static class Test16 {
        @com.example.unit.api.AfterClass
        void test1() {
            throw new IllegalStateException();
        }
    }

    @Test
    void testNonStaticAfterClassProhibited() {
        assertThrows(UnitTestRunnerException.class, () -> new UnitTestRunner(Test16.class));
    }

    static class Test17 {
        @com.example.unit.api.Before
        static void test1() {
            throw new IllegalStateException();
        }
    }
    @Test
    void testStaticBeforeProhibited() {
        assertThrows(UnitTestRunnerException.class, () -> new UnitTestRunner(Test17.class));
    }

    static class Test18 {
        @com.example.unit.api.After
        static void test1() {
            throw new IllegalStateException();
        }
    }
    @Test
    void testStaticAfterProhibited() {
        assertThrows(UnitTestRunnerException.class, () -> new UnitTestRunner(Test18.class));
    }

    static class Test19 {
        @com.example.unit.api.Test
        static void test1() {
            throw new IllegalStateException();
        }
    }
    @Test
    void testStaticTestProhibited() {
        assertThrows(UnitTestRunnerException.class, () -> new UnitTestRunner(Test19.class));
    }

    class Test20 {
        @com.example.unit.api.Test
        void test1() {
            throw new IllegalStateException();
        }
    }
    @Test
    void testInnerNoGoodConstructorProhibited() {
        assertThrows(UnitTestRunnerException.class, () -> new UnitTestRunner(Test20.class));
    }

    class Test21 {
        private Test21() {
        }

        @com.example.unit.api.Test
        void test1() {
            throw new IllegalStateException();
        }
    }

    @Test
    void testPrivateNoGoodConstructorProhibited() {
        assertThrows(UnitTestRunnerException.class, () -> new UnitTestRunner(Test21.class));
    }

    @Test
    void testResultStrings() {
        var stringsExpected = new String[]{
            "test1 took \\d+ ms, it's result is SUCCESS \\(Everything's fine\\)",
            "test2 took \\d+ ms, it's result is IGNORED \\(Don't feel like it\\)",
            "test3 took \\d+ ms, it's result is FAILURE \\(The expected exception was not thrown\\)",
            "test4 took \\d+ ms, it's result is FAILURE \\(java.lang.IllegalStateException expected, but java.lang.IllegalCallerException was thrown\\)",
            "test5 took \\d+ ms, it's result is SUCCESS \\(Caught the expected exception\\)",
            "test6 took \\d+ ms, it's result is FAILURE \\(No exception expected, but java.lang.IllegalCallerException was thrown\\)"
        };

        assertResultStringsMatch(stringsExpected, new UnitTestRunner(TestTest.class).runTests());
    }

    private void assertResultListEquals(@NonNull TestRunResult[] expectedResults, @NonNull List<TestRunResult> results) {
        Arrays.sort(expectedResults, Comparator.comparing(TestRunResult::getTestName));
        results.sort(Comparator.comparing(TestRunResult::getTestName));
        assertIterableEquals(List.of(expectedResults), results);
    }

    private void assertResultStringsMatch(@NonNull String[] expectedStringsRegexps, @NonNull List<TestRunResult> results) {
        var resultStrings = results.stream().map(TestRunResult::toString).sorted().collect(Collectors.toList());
        assertLinesMatch(List.of(expectedStringsRegexps), resultStrings);
    }
}