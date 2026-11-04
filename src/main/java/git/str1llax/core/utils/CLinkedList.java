package git.str1llax.core.utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CLinkedList<T> implements Iterable<T> {

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    private Node<T> head = null;
    private Node<T> tail = null;
    private int size = 0;

    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
            tail = newNode;
            newNode.next = head;
        } else {
            tail.next = newNode;
            tail = newNode;
            tail.next = head;
        }
        size++;
    }

    public boolean remove(T data) {
        if (head == null) return false;

        Node<T> current = head;
        Node<T> prev = tail;

        for (int i = 0; i < size; i++) {
            if (current.data.equals(data)) {
                if (size == 1) {
                    head = null;
                    tail = null;
                } else {
                    prev.next = current.next;
                    if (current == head) {
                        head = current.next;
                        tail.next = head;
                    }
                    if (current == tail) {
                        tail = prev;
                    }
                }
                size--;
                return true;
            }
            prev = current;
            current = current.next;
        }
        return false;
    }

    public void sortBy(Comparator<T> comparator) {
        if (size <= 1) return;

        boolean swapped;
        do {
            swapped = false;
            Node<T> current = head;
            for (int i = 0; i < size - 1; i++) {
                Node<T> nextNode = current.next;
                if (comparator.compare(current.data, nextNode.data) > 0) {
                    T temp = current.data;
                    current.data = nextNode.data;
                    nextNode.data = temp;
                    swapped = true;
                }
                current = nextNode;
            }
        } while (swapped);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public CircularIterator circularIterator() {
        return new CircularIterator();
    }

    @Override
    public Iterator<T> iterator() {
        return circularIterator();
    }

    public class CircularIterator implements Iterator<T> {
        private Node<T> current = head;

        @Override
        public boolean hasNext() {
            return head != null && size > 0;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            T data = current.data;
            current = current.next;
            return data;
        }

        public T current() {
            return current != null ? current.data : null;
        }
    }

}
