package com.example.treeset;

import java.util.Iterator;
import java.util.Set;

/** An interface with some of the methods of {@link java.util.NavigableSet}. */
public interface NavigableSet<E> extends Set<E> {
    /** {@link java.util.NavigableSet#descendingIterator} */
    Iterator<E> descendingIterator();

    /** {@link java.util.NavigableSet#descendingSet} */
    NavigableSet<E> descendingSet();

    /** {@link java.util.NavigableSet#first} */
    E first();

    /** {@link java.util.NavigableSet#last} */
    E last();

    /** {@link java.util.NavigableSet#lower} */
    E lower(E e);

    /** {@link java.util.NavigableSet#floor} */
    E floor(E e);

    /** {@link java.util.NavigableSet#ceiling} */
    E ceiling(E e);

    /** {@link java.util.NavigableSet#higher} */
    E higher(E e);
}