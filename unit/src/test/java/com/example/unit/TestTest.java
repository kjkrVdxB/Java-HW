package com.example.unit;

import com.example.unit.api.Test;

public class TestTest {
    public TestTest() {
    }

    @Test
    public void test1() {
    }

    @Test(ignore = "Don't feel like it")
    public void test2() {
    }

    @Test(expected = IllegalStateException.class)
    public void test3() {
    }

    @Test(expected = IllegalStateException.class)
    public void test4() {
        throw new IllegalCallerException();
    }

    @Test(expected = IllegalStateException.class)
    public void test5() {
        throw new IllegalStateException();
    }

    @Test
    public void test6() {
        throw new IllegalCallerException();
    }
}