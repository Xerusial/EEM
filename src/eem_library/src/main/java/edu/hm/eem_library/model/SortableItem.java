package edu.hm.eem_library.model;

public class SortableItem<T> implements Comparable<SortableItem<T>>{
    public final String sortableKey;
    public final T item;

    public SortableItem(String sortableKey, T item) {
        this.sortableKey = sortableKey;
        this.item = item;
    }

    @Override
    public int compareTo(SortableItem<T> o) {
        return this.sortableKey.compareToIgnoreCase(o.sortableKey);
    }
}
