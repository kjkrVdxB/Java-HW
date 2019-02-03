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
    private Node<E> root = null;

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

    @SuppressWarnings("unchecked")
    private int compare(Object a, Object b) {
        return comparator == null ? ((Comparable<? super E>) a).compareTo((E) b) : comparator.compare((E) a, (E) b);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    /** {@inheritDoc} */
    @Override
    public NavigableSet<E> descendingSet() {
        return new DescendingTreeSet<>(this);
    }

    /**
     * Add an element to the set. If the element is already preset, the set is not modified.
     *
     * @param e element to add
     * @return whether the element was added, that is it was not preset in the set before
     */
    @Override
    public boolean add(E e) {
        if (e == null) {
            throw new IllegalArgumentException("null elements are prohibited");
        }
        if (root == null) {
            root = new Node<>(null, e);
            ++size;
            ++modificationCount;
            return true;
        }
        var current = root;
        while (true) {
            int cmp = compare(e, current.element);
            if (cmp > 0) {
                if (current.rightChild == null) {
                    current.rightChild = new Node<>(current, e);
                    ++size;
                    ++modificationCount;
                    return true;
                }
                current = current.rightChild;
            } else if (cmp == 0) {
                return false;
            } else {
                if (current.leftChild == null) {
                    current.leftChild = new Node<>(current, e);
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
     * @throws IllegalArgumentException if the specified element is {@code null}
     */
    @Override
    public boolean contains(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("null elements are prohibited");
        }
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

    /** {@inheritDoc} */
    @Override
    public E lower(E e) {
        var current = root;
        E lower = null;
        while (current != null) {
            int cmp = compare(e, current.element);
            if (cmp > 0) {
                if (lower == null || compare(current.element, lower) > 0) {
                    lower = current.element;
                }
                current = current.rightChild;
            } else {
                current = current.leftChild;
            }
        }
        return lower;
    }

    /** {@inheritDoc} */
    @Override
    public E floor(E e) {
        var current = root;
        E floor = null;
        while (current != null) {
            int cmp = compare(e, current.element);
            if (cmp > 0) {
                if (floor == null || compare(current.element, floor) > 0) {
                    floor = current.element;
                }
                current = current.rightChild;
            } else if (cmp == 0) {
                return current.element;
            } else {
                current = current.leftChild;
            }
        }
        return floor;
    }

    /** {@inheritDoc} */
    @Override
    public E ceiling(E e) {
        var current = root;
        E ceiling = null;
        while (current != null) {
            int cmp = compare(e, current.element);
            if (cmp < 0) {
                if (ceiling == null || compare(current.element, ceiling) < 0) {
                    ceiling = current.element;
                }
                current = current.leftChild;
            } else if (cmp == 0) {
                return current.element;
            } else {
                current = current.rightChild;
            }
        }
        return ceiling;
    }

    @Override
    public E higher(E e) {
        var current = root;
        E higher = null;
        while (current != null) {
            int cmp = compare(e, current.element);
            if (cmp < 0) {
                if (higher == null || compare(current.element, higher) < 0) {
                    higher = current.element;
                }
                current = current.leftChild;
            } else {
                current = current.rightChild;
            }
        }
        return higher;
    }

    /**
     * Get least element in the tree rooted in {@code treeRoot}. Returns {@code null}
     * if {@code treeRoot} is {@code null}.
     */
    private Node<E> getLeast(Node<E> treeRoot) {
        if (treeRoot == null) {
            return null;
        }
        Node<E> current = treeRoot;
        while (current.leftChild != null) {
            current = current.leftChild;
        }
        return current;
    }

    /**
     * Get lbiggest element in the tree rooted in {@code treeRoot}. Returns {@code null}
     * if {@code treeRoot} is {@code null}.
     */
    private Node<E> getBiggest(Node<E> treeRoot) {
        if (treeRoot == null) {
            return null;
        }
        Node<E> current = treeRoot;
        while (current.rightChild != null) {
            current = current.rightChild;
        }
        return current;
    }

    /** Remove an element in subtree rooted at {@code subtreeRoot}. Returns {@code true} if an element was deleted. */
    private boolean removeInSubtree(Object o, Node<E> subtreeRoot) {
        var current = subtreeRoot;
        while (current != null) {
            int cmp = compare(o, current.element);
            if (cmp > 0) {
                current = current.rightChild;
            } else if (cmp == 0) {
                Node<E> newCurrent;
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
     * @throws IllegalArgumentException if the element specified is {@code null}
     */
    @Override
    public boolean remove(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("null elements are prohibited");
        }
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

    private static class Node<NodeE> {
        private Node<NodeE> parent;
        private Node<NodeE> leftChild = null, rightChild = null;
        private final NodeE element;

        private Node(Node<NodeE> parent, NodeE element) {
            this.parent = parent;
            this.element = element;
        }

        private void replaceInParent(Node<NodeE> newNode) {
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
        private int acceptedModificationCount = modificationCount;
        private Node<E> nextNode;
        private Node<E> previousNode; // used in remove()

        private TreeSetIterator() {
            previousNode = null;
            nextNode = getLeast(root);
        }

        private void checkConcurrentAccess() {
            if (modificationCount != acceptedModificationCount) {
                throw new ConcurrentModificationException("TreeSet was modified");
            }
        }

        @Override
        public boolean hasNext() {
            checkConcurrentAccess();
            return nextNode != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException("There is no next element");
            }
            checkConcurrentAccess();
            previousNode = nextNode;
            var nextElement = nextNode.element;
            if (nextNode.rightChild != null) {
                nextNode = getLeast(nextNode.rightChild);
            } else {
                while (nextNode.parent != null && nextNode.parent.rightChild == nextNode) {
                    nextNode = nextNode.parent;
                }
                nextNode = nextNode.parent;
            }
            return nextElement;
        }

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

    /** A class representing descending version of a set. */
    private static class DescendingTreeSet <E> extends AbstractSet<E> implements NavigableSet<E> {
        private TreeSet<E> inner;

        private DescendingTreeSet(TreeSet<E> inner) {
            this.inner = inner;
        }

        /** {@link TreeSet#add} */
        @Override
        public boolean add(E e) {
            return inner.add(e);
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<E> descendingIterator() {
            return inner.iterator();
        }

        /** {@inheritDoc} */
        @Override
        public NavigableSet<E> descendingSet() {
            return inner;
        }

        /** {@inheritDoc} */
        @Override
        public E first() {
            return inner.last();
        }

        /** {@inheritDoc} */
        @Override
        public E last() {
            return inner.first();
        }

        /** {@link TreeSet#contains} */
        @Override
        public boolean contains(Object o) {
            return inner.contains(o);
        }

        /** {@inheritDoc} */
        @Override
        public E lower(E e) {
            return inner.higher(e);
        }

        /** {@inheritDoc} */
        @Override
        public E floor(E e) {
            return inner.ceiling(e);
        }

        /** {@inheritDoc} */
        @Override
        public E ceiling(E e) {
            return inner.floor(e);
        }

        /** {@inheritDoc} */
        @Override
        public E higher(E e) {
            return inner.lower(e);
        }

        /** {@link TreeSet#remove} */
        @Override
        public boolean remove(Object o) {
            return inner.remove(o);
        }

        /** {@link TreeSet#iterator} */
        public Iterator<E> iterator() {
            return new DescendingTreeSetIterator();
        }

        /** {@link TreeSet#size()} */
        @Override
        public int size() {
            return inner.size();
        }

        private class DescendingTreeSetIterator implements Iterator<E> {
            private int acceptedModificationCount = inner.modificationCount;
            private TreeSet.Node<E> nextNode;
            private TreeSet.Node<E> previousNode; // used in remove()

            private DescendingTreeSetIterator() {
                previousNode = null;
                nextNode = inner.getBiggest(inner.root);
            }

            private void checkConcurrentAccess() {
                if (inner.modificationCount != acceptedModificationCount) {
                    throw new ConcurrentModificationException("TreeSet was modified");
                }
            }

            @Override
            public boolean hasNext() {
                checkConcurrentAccess();
                return nextNode != null;
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("There is no next element");
                }
                checkConcurrentAccess();
                previousNode = nextNode;
                var nextElement = nextNode.element;
                if (nextNode.leftChild != null) {
                    nextNode = inner.getBiggest(nextNode.leftChild);
                } else {
                    while (nextNode.parent != null && nextNode.parent.leftChild == nextNode) {
                        nextNode = nextNode.parent;
                    }
                    nextNode = nextNode.parent;
                }
                return nextElement;
            }

            @Override
            public void remove() {
                if (previousNode == null) {
                    throw new IllegalStateException("remove() already called after last call " +
                            "to next() or no call to next() was made");
                }
                checkConcurrentAccess();
                inner.remove(previousNode.element);
                acceptedModificationCount = inner.modificationCount;
                previousNode = null;
            }
        }
    }
}
