package git.str1llax.core.utils;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CLinkedList<T> extends LinkedList<T> {

    private final List<WeakReference<CircularIterator>> activeIterators = new ArrayList<>();

    public CLinkedList() {
        super();
    }

    public CLinkedList(Collection<? extends T> c) {
        super(c);
    }

    public CircularIterator circularIterator() {
        CircularIterator iterator = new CircularIterator();
        activeIterators.add(new WeakReference<>(iterator));
        return iterator;
    }

    public void sortBy(Comparator<? super T> comparator) {
        super.sort(comparator);
        forEachActiveIterator(CircularIterator::onListSorted);
    }

    public List<T> toList() {
        return List.copyOf(this);
    }

    @Override
    public T remove(int index) {
        T removed = super.remove(index);
        forEachActiveIterator(it -> it.onElementRemoved(index));
        return removed;
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index == -1) return false;
        remove(index);
        return true;
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        for (int i = size() - 1; i >= 0; i--) {
            if (filter.test(get(i))) {
                remove(i);
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public void clear() {
        super.clear();
        forEachActiveIterator(CircularIterator::reset);
    }

    private void forEachActiveIterator(Consumer<CircularIterator> action) {
        activeIterators.removeIf(ref -> ref.get() == null);
        for (WeakReference<CircularIterator> ref : activeIterators) {
            CircularIterator it = ref.get();
            if (it != null) {
                action.accept(it);
            }
        }
    }

    public class CircularIterator implements Iterator<T> {
        private int cursor = 0;
        private int lastReturnedIndex = -1;
        private T lastReturned = null;

        @Override
        public boolean hasNext() {
            return !isEmpty();
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException("Circular list is empty");
            if (cursor >= size()) cursor = 0;

            lastReturnedIndex = cursor;
            lastReturned = get(cursor);
            cursor = (cursor + 1) % size();
            return lastReturned;
        }

        /**
         * Returns the upcoming element at the cursor without advancing the turn.
         */
        public T peek() {
            if (isEmpty()) return null;
            if (cursor >= size()) cursor = 0;
            return get(cursor);
        }

        /**
         * Returns the element most recently returned by next() (the currently active entity).
         */
        public T current() {
            return lastReturned;
        }

        /**
         * Safely removes the element most recently returned by next() without skipping the next turn.
         */
        @Override
        public void remove() {
            if (lastReturnedIndex < 0 || lastReturnedIndex >= size()) {
                throw new IllegalStateException("next() must be called before remove()");
            }
            CLinkedList.this.remove(lastReturnedIndex);
        }

        /**
         * Removes the upcoming element at the cursor position.
         */
        public T removeUpcoming() {
            if (isEmpty()) throw new NoSuchElementException("Circular list is empty");
            if (cursor >= size()) cursor = 0;
            return CLinkedList.this.remove(cursor);
        }

        /**
         * Traverses one full cycle starting from the current cursor position.
         */
        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            int count = size();
            for (int i = 0; i < count && !isEmpty(); i++) {
                action.accept(next());
            }
        }

        public void reset() {
            cursor = 0;
            lastReturnedIndex = -1;
            lastReturned = null;
        }

        private void onElementRemoved(int removedIndex) {
            if (isEmpty()) {
                reset();
                return;
            }
            if (removedIndex < cursor) {
                cursor--;
            }
            if (cursor >= size()) {
                cursor = 0;
            }
            if (removedIndex == lastReturnedIndex) {
                lastReturned = null;
                lastReturnedIndex = -1;
            } else if (removedIndex < lastReturnedIndex) {
                lastReturnedIndex--;
            }
        }

        private void onListSorted() {
            if (isEmpty()) {
                reset();
                return;
            }
            if (lastReturned != null) {
                int newIndex = indexOf(lastReturned);
                if (newIndex != -1) {
                    lastReturnedIndex = newIndex;
                    cursor = (newIndex + 1) % size();
                    return;
                }
            }
            if (cursor >= size()) {
                cursor = 0;
            }
        }
    }
}