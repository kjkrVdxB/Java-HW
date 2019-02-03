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
    int modificationCount = 0;
    private int size = 0;
    Node<E> root = null;

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

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

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
                if (current.secondChild == null) {
                    current.secondChild = new Node<>(current, e);
                    ++size;
                    ++modificationCount;
                    return true;
                }
                current = current.secondChild;
            } else if (cmp == 0) {
                return false;
            } else {
                if (current.firstChild == null) {
                    current.firstChild = new Node<>(current, e);
                    ++size;
                    ++modificationCount;
                    return true;
                }
                current = current.firstChild;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public E first() {
        if (size() == 0) {
            throw new NoSuchElementException("TreeSet is empty");
        }
        var current = root;
        while (current.firstChild != null) {
            current = current.firstChild;
        }
        return current.element;
    }

    /** {@inheritDoc} */
    @Override
    public E last() {
        if (size() == 0) {
            throw new NoSuchElementException("TreeSet is empty");
        }
        var current = root;
        while (current.secondChild != null) {
            current = current.secondChild;
        }
        return current.element;
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
                current = current.secondChild;
            } else {
                current = current.firstChild;
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
                current = current.secondChild;
            } else {
                current = current.firstChild;
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
                current = current.secondChild;
            } else if (cmp == 0) {
                return current.element;
            } else {
                current = current.firstChild;
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
                current = current.firstChild;
            } else if (cmp == 0) {
                return current.element;
            } else {
                current = current.secondChild;
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
                current = current.firstChild;
            } else {
                current = current.secondChild;
            }
        }
        return higher;
    }

    /**
     * Get least element in the tree rooted in {@code treeRoot}. Returns {@code null}
     * if {@code treeRoot} is {@code null}.
     */
    Node<E> getLeast(Node<E> treeRoot) {
        if (treeRoot == null) {
            return null;
        }
        Node<E> current = treeRoot;
        while (current.firstChild != null) {
            current = current.firstChild;
        }
        return current;
    }

    /**
     * Get lbiggest element in the tree rooted in {@code treeRoot}. Returns {@code null}
     * if {@code treeRoot} is {@code null}.
     */
    Node<E> getBiggest(Node<E> treeRoot) {
        if (treeRoot == null) {
            return null;
        }
        Node<E> current = treeRoot;
        while (current.secondChild != null) {
            current = current.secondChild;
        }
        return current;
    }

    /** Remove an element in subtree rooted at {@code subtreeRoot}. Returns {@code true} if an element was deleted. */
    private boolean removeInSubtree(Object o, Node<E> subtreeRoot) {
        var current = subtreeRoot;
        while (current != null) {
            int cmp = compare(o, current.element);
            if (cmp > 0) {
                current = current.secondChild;
            } else if (cmp == 0) {
                Node<E> newCurrent;
                if (current.firstChild != null && current.secondChild != null) {
                    var replacement = getLeast(current.secondChild);
                    removeInSubtree(replacement.element, replacement);
                    replacement.firstChild = current.firstChild;
                    if (current.secondChild != null) {
                        current.secondChild.parent = replacement;
                    }
                    replacement.secondChild = current.secondChild;
                    if (current.firstChild != null) {
                        current.firstChild.parent = replacement;
                    }
                    current.replaceInParent(replacement);
                    if (current == root) {
                        root = replacement;
                    }
                    return true;
                } else if (current.firstChild != null) {
                    newCurrent = current.firstChild;
                } else if (current.secondChild != null) {
                    newCurrent = current.secondChild;
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
                current = current.firstChild;
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

    static class Node<NodeE> {
        Node<NodeE> parent;
        Node<NodeE> firstChild = null, secondChild = null;
        final NodeE element;

        private Node(Node<NodeE> parent, NodeE element) {
            this.parent = parent;
            this.element = element;
        }

        private void replaceInParent(Node<NodeE> newNode) {
            if (parent != null) {
                if (this == parent.firstChild) {
                    parent.firstChild = newNode;
                } else {
                    parent.secondChild = newNode;
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
            if (nextNode.secondChild != null) {
                nextNode = getLeast(nextNode.secondChild);
            } else {
                while (nextNode.parent != null && nextNode.parent.secondChild == nextNode) {
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
}
