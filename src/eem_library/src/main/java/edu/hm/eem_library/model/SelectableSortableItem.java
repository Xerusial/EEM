package edu.hm.eem_library.model;

public class SelectableSortableItem<T> extends SortableItem<T>{
    public boolean selected;

    public SelectableSortableItem(String sortableKey, T item) {
        super(sortableKey, item);
        selected =false;
    }
}
