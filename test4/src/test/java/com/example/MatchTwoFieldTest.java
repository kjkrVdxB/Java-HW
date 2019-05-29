package com.example;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
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
    void testInappropriateField() {
        assertFalse(checkFieldIsAppropriate(new int[][]{{1, 1}, {1, 2}}));
        assertTrue(checkFieldIsAppropriate(new int[][]{{1, 1}, {2, 2}}));
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

}