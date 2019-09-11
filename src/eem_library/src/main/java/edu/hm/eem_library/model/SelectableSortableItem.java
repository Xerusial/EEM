package edu.hm.eem_library.model;

/**
 * Adds seleted property to {@link SortableItem}
 *
 * @param <T> item for Sortableitem container
 */
public class SelectableSortableItem<T> extends SortableItem<T> {
    public boolean selected;

    public SelectableSortableItem(String sortableKey, T item) {
        super(sortableKey, item);
        selected = false;
    }
}
