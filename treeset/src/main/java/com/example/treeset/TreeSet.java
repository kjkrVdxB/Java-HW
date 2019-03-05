package com.example.treeset;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Binary search tree based TreeSet implementation.
 *
 * @param <E> element type
 */
public class TreeSet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final Comparator<? super E> comparator;
    private int modificationCount = 0;
    private int size = 0;
    private Node root = null;

    private final String NULL_ARGUMENT_EXCEPTION_MESSAGE = "null elements are prohibited when natural ordering is used";

    /** Create new TreeSet with natural ordering. */
    public TreeSet() {
        comparator = null;
    }

    /**
     * Create new TreeSet ordered by {@code comparator}. If {@code comparator} is {@code null},
     * natural ordering is used.
     */
    public TreeSet(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    /** Compare using comparator if there is one, or using natural ordering otherwise. */
    @SuppressWarnings("unchecked")
    private int compare(Object a, E b) {
        return comparator == null ? ((Comparable<? super E>) a).compareTo(b) : comparator.compare((E) a, b);
    }

    private int compare(Object a, E b, Direction direction) {
        return compare(a, b) * (direction == Direction.NORMAL ? 1 : -1);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    /** {@inheritDoc} */
    @Override
    public NavigableSet<E> descendingSet() {
        return new DescendingTreeSet();
    }

    private void checkNull(Object o) {
        if (o != null) {
            return;
        }
        if (comparator == null) {
            throw new NullPointerException(NULL_ARGUMENT_EXCEPTION_MESSAGE);
        }
        compare(null, null);
    }

    /**
     * Add an element to the set. If the element is already preset, the set is not modified.
     *
     * @param e element to add
     * @return whether the element was added, that is it was not preset in the set before
     * @throws ClassCastException   if the specified element cannot be compared with the elements currently in the set
     * @throws NullPointerException if the specified element is {@code null} and this set uses natural ordering,
     *                              or its comparator does not permit {@code null} elements
     */
    @Override
    public boolean add(E e) {
        checkNull(e);
        if (root == null) {
            root = new Node(null, e);
            ++size;
            ++modificationCount;
            return true;
        }
        var current = root;
        while (true) {
            int cmp = compare(e, current.element);
            if (cmp > 0) {
                if (current.rightChild == null) {
                    current.rightChild = new Node(current, e);
                    ++size;
                    ++modificationCount;
                    return true;
                }
                current = current.rightChild;
            } else if (cmp == 0) {
                return false;
            } else {
                if (current.leftChild == null) {
                    current.leftChild = new Node(current, e);
                    ++size;
                    ++modificationCount;
                    return true;
                }
                current = current.leftChild;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public E first() {
        if (size() == 0) {
            throw new NoSuchElementException("TreeSet is empty");
        }
        return getLeast(root).element;
    }

    /** {@inheritDoc} */
    @Override
    public E last() {
        if (size() == 0) {
            throw new NoSuchElementException("TreeSet is empty");
        }
        return getBiggest(root).element;
    }

    /**
     * Returns true if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested
     * @return true if this set contains the specified element
     * @throws ClassCastException   if the specified element cannot be compared with the elements currently in the set
     * @throws NullPointerException if the specified element is {@code null} and this set uses natural ordering,
     *                              or its comparator does not permit {@code null} elements
     */
    @Override
    public boolean contains(Object o) {
        checkNull(o);
        var current = root;
        while (current != null) {
            int cmp = compare(o, current.element);
            if (cmp == 0) {
                break;
            } else if (cmp > 0) {
                current = current.rightChild;
            } else {
                current = current.leftChild;
            }
        }
        return current != null;
    }

    /**
     * Find the element before given, according to {@code direction}, possibly equal to given if
     * {@code onEqual} is {@code OnEqual.ACCEPT}.
     */
    private E before(E e, OnEqual onEqual, Direction direction) {
        assert onEqual != null;
        assert direction != null;
        checkNull(e);
        var current = root;
        E before = null;
        while (current != null) {
            int cmp = compare(e, current.element, direction);
            if (cmp > 0) {
                if (before == null || compare(current.element, before, direction) > 0) {
                    before = current.element;
                }
                current = current.biggerChild(direction);
            } else if (cmp == 0 && onEqual == OnEqual.ACCEPT) {
                return current.element;
            } else {
                current = current.smallerChild(direction);
            }
        }
        return before;
    }

    /** {@inheritDoc} */
    @Override
    public E lower(E e) {
        return before(e, OnEqual.REJECT, Direction.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    public E floor(E e) {
        return before(e, OnEqual.ACCEPT, Direction.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    public E ceiling(E e) {
        return before(e, OnEqual.ACCEPT, Direction.REVERSED);
    }

    /** {@inheritDoc} */
    @Override
    public E higher(E e) {
        return before(e, OnEqual.REJECT, Direction.REVERSED);
    }

    /**
     * Get least element in the tree rooted in {@code treeRoot}, according to {@code direction}. Returns {@code null}
     * if {@code treeRoot} is {@code null}.
     */
    private Node getLeast(Node treeRoot, Direction direction) {
        if (treeRoot == null) {
            return null;
        }
        Node current = treeRoot;
        while (current.smallerChild(direction) != null) {
            current = current.smallerChild(direction);
        }
        return current;
    }

    /**
     * Get least element in the tree rooted in {@code treeRoot}. Returns {@code null}
     * if {@code treeRoot} is {@code null}.
     */
    private Node getLeast(Node treeRoot) {
        return getLeast(treeRoot, Direction.NORMAL);
    }

    /**
     * Get biggest element in the tree rooted in {@code treeRoot}. Returns {@code null}
     * if {@code treeRoot} is {@code null}.
     */
    private Node getBiggest(Node treeRoot) {
        return getLeast(treeRoot, Direction.REVERSED);
    }

    /** Remove an element in subtree rooted at {@code subtreeRoot}. Returns {@code true} if an element was deleted. */
    private boolean removeInSubtree(Object o, Node subtreeRoot) {
        var current = subtreeRoot;
        while (current != null) {
            int cmp = compare(o, current.element);
            if (cmp != 0) {
                current = cmp < 0 ? current.leftChild : current.rightChild;
                continue;
            }
            Node newCurrent = null;
            if (current.leftChild != null && current.rightChild != null) {
                var replacement = getLeast(current.rightChild);
                removeInSubtree(replacement.element, replacement);
                current.replaceWith(replacement);
                return true;
            } else if (current.leftChild != null) {
                newCurrent = current.leftChild;
            } else if (current.rightChild != null) {
                newCurrent = current.rightChild;
            }
            current.replaceInParent(newCurrent);
            if (current == root) {
                root = newCurrent;
            }
            --size;
            ++modificationCount;
            return true;
        }
        return false;
    }

    /**
     * Removes the instance of the specified element from this set, if it is present.
     * Returns true if this collection contained the specified element (or equivalently,
     * if this collection changed as a result of the call).
     *
     * @param o element to be removed from this set, if present
     * @return true if an element was removed as a result of this call
     * @throws ClassCastException   if the specified element cannot be compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null and this set uses natural ordering,
     *                              or its comparator does not permit null elements
     */
    @Override
    public boolean remove(Object o) {
        checkNull(o);
        return removeInSubtree(o, root);
    }

    /** Returns iterator over the set. */
    @Override
    public Iterator<E> iterator() {
        return new TreeSetIterator();
    }

    /** Returns number of elements in the set. */
    @Override
    public int size() {
        return size;
    }

    private enum OnEqual {
        ACCEPT,
        REJECT,
    }

    private enum Direction {
        NORMAL,
        REVERSED
    }

    private class Node {
        private Node parent;
        private Node leftChild = null;
        private Node rightChild = null;
        private final E element;

        private Node(Node parent, E element) {
            this.parent = parent;
            this.element = element;
        }

        /** Replace the parent's child corresponding to this node with {@code newNode} */
        private void replaceInParent(Node newNode) {
            if (parent != null) {
                if (this == parent.leftChild) {
                    parent.leftChild = newNode;
                } else {
                    parent.rightChild = newNode;
                }
            }
            if (newNode != null) {
                newNode.parent = parent;
            }
        }

        /**
         * Replace this node with another node completely, including information in children.
         * Also update root in case it was this node.
         */
        private void replaceWith(Node newNode) {
            assert newNode != null;
            newNode.leftChild = leftChild;
            if (rightChild != null) {
                rightChild.parent = newNode;
            }
            newNode.rightChild = rightChild;
            if (leftChild != null) {
                leftChild.parent = newNode;
            }
            replaceInParent(newNode);
            if (this == root) {
                root = newNode;
            }
        }

        /** Returns smaller child according to {@code direction}. */
        private Node smallerChild(Direction direction) {
            assert direction != null;
            return direction == Direction.NORMAL ? leftChild : rightChild;
        }

        /** Returns bigger child according to {@code direction}. */
        private Node biggerChild(Direction direction) {
            assert direction != null;
            return direction == Direction.NORMAL ? rightChild : leftChild;
        }
    }

    private class TreeSetIterator implements Iterator<E> {
        /** Tree modification count that this iterator expects to see, so it can detexd concurrent access. */
        private int acceptedModificationCount;
        /**
         * Node that is previous to {@code nextNode} in the order. Used in implementation of {@code remove()}
         * method.
         */
        private Node previousNode;
        private Direction direction;
        private Node nextNode;

        private TreeSetIterator() {
            this(Direction.NORMAL);
        }

        private TreeSetIterator(Direction direction) {
            assert direction != null;
            acceptedModificationCount = modificationCount;
            previousNode = null;
            this.direction = direction;
            nextNode = getLeast(root, direction);
        }

        /** Check whether the set was modified by something other that this iterator */
        void checkConcurrentAccess() {
            if (modificationCount != acceptedModificationCount) {
                throw new ConcurrentModificationException("TreeSet was modified");
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            checkConcurrentAccess();
            return nextNode != null;
        }

        /** {@inheritDoc} */
        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException("There is no next element");
            }
            checkConcurrentAccess();
            previousNode = nextNode;
            var nextElement = nextNode.element;
            if (nextNode.biggerChild(direction) != null) {
                nextNode = getLeast(nextNode.biggerChild(direction), direction);
            } else {
                while (nextNode.parent != null && nextNode.parent.biggerChild(direction) == nextNode) {
                    nextNode = nextNode.parent;
                }
                nextNode = nextNode.parent;
            }
            return nextElement;
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            if (previousNode == null) {
                throw new IllegalStateException("remove() already called after last call " +
                                                "to next() or no call to next() was made");
            }
            checkConcurrentAccess();
            TreeSet.this.remove(previousNode.element);
            acceptedModificationCount = modificationCount;
            previousNode = null;
        }
    }

    /**
     * A class representing descending version of a set.
     */
    private final class DescendingTreeSet extends AbstractSet<E> implements NavigableSet<E> {
        private DescendingTreeSet() { }

        /** {@link TreeSet#add} */
        @Override
        public boolean add(E e) {
            return TreeSet.this.add(e);
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<E> descendingIterator() {
            return TreeSet.this.iterator();
        }

        /** {@inheritDoc} */
        @Override
        public NavigableSet<E> descendingSet() {
            return TreeSet.this;
        }

        /** {@inheritDoc} */
        @Override
        public E first() {
            return TreeSet.this.last();
        }

        /** {@inheritDoc} */
        @Override
        public E last() {
            return TreeSet.this.first();
        }

        /** {@link TreeSet#contains} */
        @Override
        public boolean contains(Object o) {
            return TreeSet.this.contains(o);
        }

        /** {@inheritDoc} */
        @Override
        public E lower(E e) {
            return TreeSet.this.higher(e);
        }

        /** {@inheritDoc} */
        @Override
        public E floor(E e) {
            return TreeSet.this.ceiling(e);
        }

        /** {@inheritDoc} */
        @Override
        public E ceiling(E e) {
            return TreeSet.this.floor(e);
        }

        /** {@inheritDoc} */
        @Override
        public E higher(E e) {
            return TreeSet.this.lower(e);
        }

        /** {@link TreeSet#remove} */
        @Override
        public boolean remove(Object o) {
            return TreeSet.this.remove(o);
        }

        /** {@link TreeSet#iterator} */
        public Iterator<E> iterator() {
            return new TreeSetIterator(Direction.REVERSED);
        }

        /** {@link TreeSet#size()} */
        @Override
        public int size() {
            return TreeSet.this.size();
        }
    }
}
