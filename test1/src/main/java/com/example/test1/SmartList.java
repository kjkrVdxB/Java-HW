package com.example.test1;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** ArrayList analog with smart storage. */
public class SmartList<E> extends AbstractList<E> implements List<E> {
    private int size;
    private Object link;
    private static int SIZE_BOUND = 5;

    /** Creates empty SmartList. */
    public SmartList() {
        size = 0;
        link = null;
    }

    /** Creates new SmartList based on the given collection. */
    @SuppressWarnings("unchecked")
    public SmartList(Collection<? extends E> c) {
        size = c.size();
        if (c.isEmpty()) {
            link = null;
        } else if (c.size() == 1) {
            link = c.toArray()[0];
        } else if (c.size() <= 5) {
            link = (E[]) new Object[5];
            System.arraycopy(c.toArray(), 0, link, 0, c.size());
        } else {
            link = new ArrayList<E>(c);
        }
    }

    /** Returns the element at the specified position in this list. */
    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        if (size == 1) {
            return (E) link;
        } else if (size <= 5) {
            return ((E[]) link)[index];
        } else {
            return ((ArrayList<E>) link).get(index);
        }
    }

    /** Returns the number of elements in this list. */
    @Override
    public int size() {
        return size;
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     *
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
     */
    @SuppressWarnings("unchecked")
    @Override
    public E set(int index, E element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        var previous = get(index);
        if (size == 1) {
            link = element;
        } else if (size <= 5) {
            ((E[]) link)[index] = element;
        } else { // size >= 6
            ((ArrayList<E>) link).set(index, element);
        }
        return previous;
    }

    /**
     * Inserts the specified element at the specified position in this list (optional operation). Shifts the element
     * currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     *
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > size())
     */
    @SuppressWarnings("unchecked")
    @Override
    public void add(int index, E element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
        if (size == 0) {
            link = element;
        } else if (size == 1) {
            var tmpArray = (E[])(new Object[5]);
            tmpArray[index] = element;
            tmpArray[1 - index] = (E) link;
            link = tmpArray;
        } else if (size <= 4) {
            if (size - index >= 0) {
                System.arraycopy(((E[]) link), index, ((E[]) link), index + 1, size - index);
            }
            ((E[]) link)[index] = element;
        } else if (size == 5) {
            link = new ArrayList<E>(Arrays.asList((E[]) link));
            ((ArrayList<E>) link).add(index, element);
        } else { // size >= 6
            ((ArrayList<E>) link).add(index, element);
        }
        ++size;
    }

    /**
     * Removes the element at the specified position in this list (optional operation). Shifts any subsequent elements
     * to the left (subtracts one from their indices). Returns the element that was removed from the list.
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     *
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
     */
    @SuppressWarnings("unchecked")
    @Override
    public E remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        E previous;
        if (size >= SIZE_BOUND + 2) {
            previous = ((ArrayList<E>) link).remove(index);
        } else if (size == SIZE_BOUND + 1) {
            previous = ((ArrayList<E>) link).remove(index);
            var tmpArray = ((ArrayList<E>) link).toArray();
            link = (E[]) new Object[5];
            System.arraycopy(tmpArray, 0, link, 0, SIZE_BOUND);
        } else if (size >= 3) {
            previous = ((E[]) link)[index];
            for (int i = index+1; i < size; ++i) {
                ((E[]) link)[i - 1] = ((E[]) link)[i];
            }
        } else if (size == 2) {
            previous = ((E[]) link)[index];
            link = ((E[]) link)[1 - index];
        } else { // size == 1
            previous = (E) link;
            link = null;
        }
        --size;
        return previous;
    }
}