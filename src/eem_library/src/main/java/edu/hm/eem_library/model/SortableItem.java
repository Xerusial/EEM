package edu.hm.eem_library.model;

public class SortableItem<S extends Comparable<? super S>, T> implements Comparable<SortableItem<S,T>>{
    final S sortableKey;
    final T item;

    SortableItem(S sortableKey, T item) {
        this.sortableKey = sortableKey;
        this.item = item;
    }

    @Override
    public int compareTo(SortableItem<S, T> o) {
        return this.sortableKey.compareTo(o.sortableKey);
    }
}
