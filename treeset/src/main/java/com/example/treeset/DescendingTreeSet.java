package com.example.treeset;

import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

class DescendingTreeSet <E> extends AbstractSet<E> implements NavigableSet<E> {
    // package-private, as it is only supposed to be used in TreeSet's descendingSet

    private TreeSet<E> inner;

    DescendingTreeSet(TreeSet<E> base) {
        inner = base;
    }

    @Override
    public boolean add(E e) {
        return inner.add(e);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return inner.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return inner;
    }

    @Override
    public E first() {
        return inner.last();
    }

    @Override
    public E last() {
        return inner.first();
    }

    @Override
    public boolean contains(Object o) {
        return inner.contains(o);
    }

    @Override
    public E lower(E e) {
        return inner.higher(e);
    }

    @Override
    public E floor(E e) {
        return inner.ceiling(e);
    }

    @Override
    public E ceiling(E e) {
        return inner.floor(e);
    }

    @Override
    public E higher(E e) {
        return inner.lower(e);
    }

    @Override
    public boolean remove(Object o) {
        return inner.remove(o);
    }

    public Iterator<E> iterator() {
        return new DescendingTreeSetIterator();
    }

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
            if (nextNode.firstChild != null) {
                nextNode = inner.getBiggest(nextNode.firstChild);
            } else {
                while (nextNode.parent != null && nextNode.parent.firstChild == nextNode) {
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
