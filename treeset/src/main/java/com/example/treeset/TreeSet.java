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
     * @throws ClassCastException if the specified element cannot be compared with the elements currently in the set
     * @throws NullPointerException if the specified element is {@code null} and this set uses natural ordering,
     * or its comparator does not permit {@code null} elements
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
     * @throws ClassCastException if the specified element cannot be compared with the elements currently in the set
     * @throws NullPointerException if the specified element is {@code null} and this set uses natural ordering,
     * or its comparator does not permit {@code null} elements
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

    enum OnEqual {
        ACCEPT,
        REJECT,
    }

    enum Direction {
        NORMAL,
        REVERSED
    }

    private E before(E e, OnEqual onEqual , Direction direction) {
        checkNull(e);
        boolean distinct = onEqual == OnEqual.REJECT;
        boolean reverse = direction == Direction.REVERSED;
        var current = root;
        E before = null;
        while (current != null) {
            int cmp = compare(e, current.element) * (reverse ? -1 : 1);
            if (cmp > 0) {
                if (before == null || compare(current.element, before) * (reverse ? -1 : 1) > 0) {
                    before = current.element;
                }
                current = reverse ? current.leftChild : current.rightChild;
            } else if (cmp == 0 && !distinct) {
                return current.element;
            } else {
                current = reverse ? current.rightChild : current.leftChild;
            }
        }
        return before;
    }

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
     * Get least element in the tree rooted in {@code treeRoot}. Returns {@code null}
     * if {@code treeRoot} is {@code null}.
     */
    private Node getLeast(Node treeRoot) {
        if (treeRoot == null) {
            return null;
        }
        Node current = treeRoot;
        while (current.leftChild != null) {
            current = current.leftChild;
        }
        return current;
    }

    /**
     * Get the biggest element in the tree rooted in {@code treeRoot}. Returns {@code null}
     * if {@code treeRoot} is {@code null}.
     */
    private Node getBiggest(Node treeRoot) {
        if (treeRoot == null) {
            return null;
        }
        Node current = treeRoot;
        while (current.rightChild != null) {
            current = current.rightChild;
        }
        return current;
    }

    /** Remove an element in subtree rooted at {@code subtreeRoot}. Returns {@code true} if an element was deleted. */
    private boolean removeInSubtree(Object o, Node subtreeRoot) {
        var current = subtreeRoot;
        while (current != null) {
            int cmp = compare(o, current.element);
            if (cmp > 0) {
                current = current.rightChild;
            } else if (cmp == 0) {
                Node newCurrent;
                if (current.leftChild != null && current.rightChild != null) {
                    var replacement = getLeast(current.rightChild);
                    removeInSubtree(replacement.element, replacement);
                    replacement.leftChild = current.leftChild;
                    if (current.rightChild != null) {
                        current.rightChild.parent = replacement;
                    }
                    replacement.rightChild = current.rightChild;
                    if (current.leftChild != null) {
                        current.leftChild.parent = replacement;
                    }
                    current.replaceInParent(replacement);
                    if (current == root) {
                        root = replacement;
                    }
                    return true;
                } else if (current.leftChild != null) {
                    newCurrent = current.leftChild;
                } else if (current.rightChild != null) {
                    newCurrent = current.rightChild;
                } else {
                    newCurrent = null;
                }
                current.replaceInParent(newCurrent);
                if (current == root) {
                    root = newCurrent;
                }
                --size;
                ++modificationCount;
                return true;
            } else {
                current = current.leftChild;
            }
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
     * @throws ClassCastException if the specified element cannot be compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null and this set uses natural ordering,
     * or its comparator does not permit null elements
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
    }

    private class TreeSetIterator implements Iterator<E> {
        private int acceptedModificationCount;
        private Node previousNode; // used in remove()
        private boolean reverse;
        private Node nextNode;

        private TreeSetIterator() {
            this(Direction.NORMAL);
        }

        private TreeSetIterator(Direction direction) {
            acceptedModificationCount = modificationCount;
            previousNode = null;
            this.reverse = direction == Direction.REVERSED;
            nextNode = reverse ? getBiggest(root) : getLeast(root);
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
            if ((reverse ? nextNode.leftChild : nextNode.rightChild) != null) {
                nextNode = reverse ? getBiggest(nextNode.leftChild) : getLeast(nextNode.rightChild);
            } else {
                while (nextNode.parent != null &&
                       (reverse ? nextNode.parent.leftChild : nextNode.parent.rightChild) == nextNode) {
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
