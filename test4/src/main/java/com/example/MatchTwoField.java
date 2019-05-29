package com.example;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;

/** Encapsulates field and "Match two" rules */
public class MatchTwoField {
    private int[][]  numbers;
    private boolean[][] opened;
    private int size;
    private int currentMatched = 0;
    private ImmutablePair<Integer, Integer> firstOpened = null;
    private ImmutablePair<Integer, Integer> secondOpened = null;

    /**
     * Generate a numbers field of given size
     *
     * @throws IllegalArgumentException if the size is not a multiple of 2
     */
    public static int[][] generateField(int size) {
        if (size % 2 != 0) {
            throw new IllegalArgumentException("MatchTwoField size should be even");
        }
        var numbers = new int[size][size];
        var numbersToShuffle = new ArrayList<Integer>(size * size);
        for (int i = 0; i < size * size; ++i) {
            numbersToShuffle.add(i / 2 + 1);
        }
        Collections.shuffle(numbersToShuffle);
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                numbers[i][j] = numbersToShuffle.get(i * size + j);
            }
        }
        return numbers;
    }

    /**
     * Check that the field is appropriate, that is a square field with numbers from 1 to (field.length)^2 / 2, each
     * of which is encountered exactly two times
     */
    public static boolean checkFieldIsAppropriate(int[] @NonNull []  field) {
        if (field == null) {
            return false;
        }
        int size = field.length;
        for (int[] ints: field) {
            if (ints == null || ints.length != size) {
                return false;
            }
        }
        int[] counter = new int[size * size / 2];
        for (int[] ints: field) {
            for (int j = 0; j < size; ++j) {
                if (ints[j] < 1 || ints[j] > size * size / 2) {
                    return false;
                }
                ++counter[ints[j] - 1];
            }
        }
        for (int i = 0; i < size; ++i) {
            if (counter[i] != 2) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create new match two game with a random field of given size (length of both the dimensions)
     *
     * @throws IllegalArgumentException if the size is not a multiple of 2
     */
    public MatchTwoField(int size) {
        this(generateField(size));
    }

    /**
     * Create new match two game with the given field
     *
     * @throws IllegalArgumentException if the field is not appropriate
     */
    public MatchTwoField(int[][] field) {
        if (!checkFieldIsAppropriate(field)) {
            throw new IllegalArgumentException("field is inappropriate");
        }
        size = field.length;
        numbers = field;
        opened = new boolean[size][size];
    }

    /**
     * Open the card at position
     *
     * @return The integer that was opened or null if the opening should not be permitted (a normal situation, nothing
     *         should happen with the displayed field in this case)
     */
    public Integer open(ImmutablePair<Integer, Integer> position) {
        if (finishedOpening() || finished() || opened[position.getLeft()][position.getRight()]) {
            return null;
        }
        if (firstOpened == null) {
            firstOpened = position;
        } else if (secondOpened == null) {
            if (position.equals(firstOpened)) {
                return null;
            }
            secondOpened = position;
        }
        return numbers[position.getLeft()][position.getRight()];
    }

    /** Checks that the opening resulted in a match */
    public boolean matched() {
        return finishedOpening() &&
               numbers[firstOpened.getLeft()][firstOpened.getRight()] == numbers[secondOpened.getLeft()][secondOpened.getRight()];
    }

    /**
     * Get the first opened position
     *
     * @throws IllegalStateException if the opening was not finished
     */
    public ImmutablePair<Integer, Integer> getFirstOpened() {
        if (!finishedOpening()) {
            throw new IllegalStateException("Opening was not finished");
        }
        return firstOpened;
    }

    /**
     * Get the second opened position
     *
     * @throws IllegalStateException if the opening was not finished
     */
    public ImmutablePair<Integer, Integer> getSecondOpened() {
        if (!finishedOpening()) {
            throw new IllegalStateException("Opening was not finished");
        }
        return secondOpened;
    }

    /** Checks whether two cards was opened */
    public boolean finishedOpening() {
        return firstOpened != null && secondOpened != null;
    }

    /** Checks whether the game has ended */
    public boolean finished() {
        return currentMatched == size * size;
    }

    /**
     * Completes the opening.
     *
     * @throws IllegalStateException if the opening did not finish
     */
    public void completeOpening() {
        if (!finishedOpening()) {
            throw new IllegalStateException("Opening not finished");
        }
        if (matched()) {
            opened[firstOpened.getLeft()][firstOpened.getRight()] = true;
            opened[secondOpened.getLeft()][secondOpened.getRight()] = true;
            currentMatched += 2;
        }
        firstOpened = null;
        secondOpened = null;
    }
}
