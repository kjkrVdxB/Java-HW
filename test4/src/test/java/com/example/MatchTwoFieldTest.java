package com.example;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static com.example.MatchTwoField.checkFieldIsAppropriate;
import static com.example.MatchTwoField.generateField;
import static org.junit.jupiter.api.Assertions.*;

class MatchTwoFieldTest {
    private MatchTwoField field;

    @BeforeEach
    void initField() {
        field = new MatchTwoField(new int[][]{{1, 1}, {2, 2}});
    }

    @Test
    void testWrongDimensions() {
        assertThrows(IllegalArgumentException.class, () -> generateField(3));
        assertThrows(IllegalArgumentException.class, () -> new MatchTwoField(3));
    }

    @Test
    void testAppropriateField() {
        assertTrue(checkFieldIsAppropriate(new int[][]{{1, 1}, {2, 2}}));
    }

    @Test
    void testInappropriateFieldRepeated() {
        assertFalse(checkFieldIsAppropriate(new int[][]{{1, 1}, {1, 2}}));
    }

    @Test
    void testInappropriateFieldTooBig() {
        assertFalse(checkFieldIsAppropriate(new int[][]{{1, 1}, {5, 2}}));
    }

    @Test
    void testMoveOutOfOrder() {
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 0)));
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 1)));
        assertNull(field.open(ImmutablePair.of(1, 0)));
        assertTrue(field.finishedOpening());
    }

    @Test
    void testMatched() {
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 0)));
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 1)));
        assertTrue(field.matched());
    }

    @Test
    void testFinishedOpening() {
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 0)));
        assertFalse(field.finishedOpening());
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 1)));
        assertTrue(field.finishedOpening());
    }

    @Test
    void testGameFinished() {
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 0)));
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 1)));
        assertTrue(field.finishedOpening());
        assertTrue(field.matched());
        field.completeOpening();
        assertEquals(2, (int)field.open(ImmutablePair.of(1, 0)));
        assertEquals(2, (int)field.open(ImmutablePair.of(1, 1)));
        assertTrue(field.finishedOpening());
        assertTrue(field.matched());
        field.completeOpening();
        assertTrue(field.finished());
    }

    @Test
    void testOpeningAlreadyOpenedAtOnce() {
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 0)));
        assertNull(field.open(ImmutablePair.of(0, 0)));
    }

    @Test
    void testOpeningAlreadyOpened() {
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 0)));
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 1)));
        assertTrue(field.finishedOpening());
        assertTrue(field.matched());
        assertNull(field.open(ImmutablePair.of(0, 0)));
    }

    @Test
    void testCompleteNotFinishedOpeningThrows() {
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 0)));
        assertThrows(IllegalStateException.class, () -> field.completeOpening());
    }

    @Test
    void testGettingOpenedOnNotFinishedOpeningThrows() {
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 0)));
        assertThrows(IllegalStateException.class, () -> field.getFirstOpened());
        assertThrows(IllegalStateException.class, () -> field.getSecondOpened());
    }

    @RepeatedTest(10)
    void stressRandomGeneration() {
        assertTrue(checkFieldIsAppropriate(generateField(4)));
    }

    @Test
    void testFieldConstructorFieldIsCloned() {
        var numbers = new int[][]{{1, 1}, {2, 2}};
        var field = new MatchTwoField(numbers);
        numbers[0][0] = 2;
        assertEquals(1, (int)field.open(ImmutablePair.of(0, 0)));
        assertEquals(2, (int)field.open(ImmutablePair.of(1, 0)));
        assertTrue(field.finishedOpening());
        assertFalse(field.matched());
    }
}