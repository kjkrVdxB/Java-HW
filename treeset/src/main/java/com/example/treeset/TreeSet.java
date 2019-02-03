package com.example.treeset;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class TreeSet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final Comparator<? super E> comparator;
    private boolean reverse = false;
    private MutableInt modificationCount = new MutableInt(0); // mutable so it can be shared among views
    private MutableInt size = new MutableInt(0); // mutable so it can be shared among views
    private MutableObject<Node<E>> root = new MutableObject<>(null); // mutable so it can be shared among views

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
        return (reverse ? -1 : 1) * (comparator == null ? ((Comparable<? super E>) a).compareTo((E) b)
                : comparator.compare((E) a, (E) b));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        var newSet = newView();
        newSet.reverse ^= true;
        return newSet;
    }

    private TreeSet<E> newView() {
        var view = new TreeSet<E>(comparator);
        view.reverse = reverse;
        view.modificationCount = modificationCount;
        view.size = size;
        view.root = root;

        return view;
    }

    private Node<E> getLeftChildOf(Node<E> node) {
        if (reverse) {
            return node.secondChild;
        } else {
            return node.firstChild;
        }
    }

    private Node<E> getRightChildOf(Node<E> node) {
        if (!reverse) {
            return node.secondChild;
        } else {
            return node.firstChild;
        }
    }

    private void setLeftChildOf(Node<E> node, Node<E> child) {
        if (reverse) {
            node.secondChild = child;
        } else {
            node.firstChild = child;
        }
    }

    private void setRightChildOf(Node<E> node, Node<E> child) {
        if (!reverse) {
            node.secondChild = child;
        } else {
            node.firstChild = child;
        }
    }

    @Override
    public boolean add(E e) {
        if (root.getValue() == null) {
            root.setValue(new Node<>(null, e));
            size.add(1);
            modificationCount.add(1);
            return true;
        }
        var current = root.getValue();
        while (true) {
            int cmp = compare(e, current.element);
            if (cmp > 0) {
                if (getRightChildOf(current) == null) {
                    setRightChildOf(current, new Node<>(current, e));
                    size.add(1);
                    modificationCount.add(1);
                    return true;
                }
                current = getRightChildOf(current);
            } else if (cmp == 0) {
                return false;
            } else {
                if (getLeftChildOf(current) == null) {
                    setLeftChildOf(current, new Node<>(current, e));
                    size.add(1);
                    modificationCount.add(1);
                    return true;
                }
                current = getLeftChildOf(current);
            }
        }
    }


    @Override
    public E first() {
        if (size() == 0) {
            throw new NoSuchElementException("TreeSet is empty");
        }
        var current = root.getValue();
        while (getLeftChildOf(current) != null) {
            current = getLeftChildOf(current);
        }
        return current.element;
    }

    @Override
    public E last() {
        if (size() == 0) {
            throw new NoSuchElementException("TreeSet is empty");
        }
        var current = root.getValue();
        while (getRightChildOf(current) != null) {
            current = getRightChildOf(current);
        }
        return current.element;
    }

    @Override
    public boolean contains(Object o) {
        var current = root.getValue();
        while (current != null) {
            int cmp = compare(o, current.element);
            if (cmp == 0) {
                break;
            } else if (cmp > 0) {
                current = getRightChildOf(current);
            } else {
                current = getLeftChildOf(current);
            }
        }
        return current != null;
    }

    @Override
    public E lower(E e) {
        var current = root.getValue();
        E lower = null;
        while (current != null) {
            int cmp = compare(e, current.element);
            if (cmp > 0) {
                if (lower == null || compare(current.element, lower) > 0) {
                    lower = current.element;
                }
                current = getRightChildOf(current);
            } else {
                current = getLeftChildOf(current);
            }
        }
        return lower;
    }

    @Override
    public E floor(E e) {
        var current = root.getValue();
        E floor = null;
        while (current != null) {
            int cmp = compare(e, current.element);
            if (cmp > 0) {
                if (floor == null || compare(current.element, floor) > 0) {
                    floor = current.element;
                }
                current = getRightChildOf(current);
            } else if (cmp == 0) {
                return current.element;
            } else {
                current = getLeftChildOf(current);
            }
        }
        return floor;
    }

    @Override
    public E ceiling(E e) {
        var current = root.getValue();
        E ceiling = null;
        while (current != null) {
            int cmp = compare(e, current.element);
            if (cmp < 0) {
                if (ceiling == null || compare(current.element, ceiling) < 0) {
                    ceiling = current.element;
                }
                current = getLeftChildOf(current);
            } else if (cmp == 0) {
                return current.element;
            } else {
                current = getRightChildOf(current);
            }
        }
        return ceiling;
    }

    @Override
    public E higher(E e) {
        var current = root.getValue();
        E higher = null;
        while (current != null) {
            int cmp = compare(e, current.element);
            if (cmp < 0) {
                if (higher == null || compare(current.element, higher) < 0) {
                    higher = current.element;
                }
                current = getLeftChildOf(current);
            } else {
                current = getRightChildOf(current);
            }
        }
        return higher;
    }

    private Node<E> getLeast(Node<E> treeRoot) {
        if (treeRoot == null) {
            return null;
        }
        Node<E> current = treeRoot;
        while (getLeftChildOf(current) != null) {
            current = getLeftChildOf(current);
        }
        return current;
    }

    private boolean removeInSubtree(Object o, Node<E> subtreeRoot) {
        var current = subtreeRoot;
        while (current != null) {
            int cmp = compare(o, current.element);
            if (cmp > 0) {
                current = getRightChildOf(current);
            } else if (cmp == 0) {
                Node<E> newCurrent;
                if (getLeftChildOf(current) != null && getRightChildOf(current) != null) {
                    var replacement = getLeast(getRightChildOf(current));
                    removeInSubtree(replacement.element, replacement);
                    setLeftChildOf(replacement, getLeftChildOf(current));
                    if (getRightChildOf(current) != null) {
                        getRightChildOf(current).parent = replacement;
                    }
                    setRightChildOf(replacement, getRightChildOf(current));
                    if (getLeftChildOf(current) != null) {
                        getLeftChildOf(current).parent = replacement;
                    }
                    current.replaceInParent(replacement);
                    if (current == root.getValue()) {
                        root.setValue(replacement);
                    }
                    return true;
                } else if (getLeftChildOf(current) != null) {
                    newCurrent = getLeftChildOf(current);
                } else if (getRightChildOf(current) != null) {
                    newCurrent = getRightChildOf(current);
                } else {
                    newCurrent = null;
                }
                current.replaceInParent(newCurrent);
                if (current == root.getValue()) {
                    root.setValue(newCurrent);
                }
                size.subtract(1);
                modificationCount.add(1);
                return true;
            } else {
                current = getLeftChildOf(current);
            }
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return removeInSubtree(o, root.getValue());
    }

    @Override
    public Iterator<E> iterator() {
        return new TreeSetIterator();
    }

    @Override
    public int size() {
        return size.intValue();
    }

    private static class Node<NodeE> {
        Node<NodeE> parent;
        Node<NodeE> firstChild = null, secondChild = null;
        NodeE element;

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
        private int acceptedModificationCount;
        private Node<E> nextNode;
        private Node<E> previousNode; // used in remove()

        private TreeSetIterator() {
            acceptedModificationCount = modificationCount.intValue();
            previousNode = null;
            nextNode = getLeast(root.getValue());
        }

        private void checkConcurrentAccess() {
            if (modificationCount.intValue() != acceptedModificationCount) {
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
            if (getRightChildOf(nextNode) != null) {
                nextNode = getLeast(getRightChildOf(nextNode));
            } else {
                while (nextNode.parent != null && getRightChildOf(nextNode.parent) == nextNode) {
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
            acceptedModificationCount = modificationCount.intValue();

            previousNode = null;
        }
    }
}
