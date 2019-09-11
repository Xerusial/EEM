package edu.hm.eem_library.model;

/**
 * Base container for all sortable data types in this project.
 *
 * @param <T> Element in need of sorting
 */
public class SortableItem<T> implements Comparable<SortableItem<T>> {
    public final T item;
    private String sortableKey;

    /**
     * Constructor
     *
     * @param sortableKey Unique name
     * @param item item to be stored with name
     */
    SortableItem(String sortableKey, T item) {
        this.sortableKey = sortableKey;
        this.item = item;
    }

    /**
     * Getter for key
     *
     * @return the unique key of the item
     */
    public String getSortableKey() {
        return sortableKey;
    }

    /**
     * If list already contains a key, this method can repeatedly be called to create new keys
     */
    void incrKey() {
        String[] parts = sortableKey.split(" ");
        int preEndIndex = parts.length - 1;
        int num;
        try {
            num = Integer.parseInt(parts[preEndIndex]);
            num++;
        } catch (NumberFormatException e) {
            num = 1;
            preEndIndex = parts.length;
        }
        sortableKey = "";
        StringBuilder builder = new StringBuilder(parts[0] + ' ');
        for (int i = 1; i < preEndIndex; i++) {
            builder.append(parts[i]);
            builder.append(' ');
        }
        builder.append(num);
        sortableKey = builder.toString();
    }

    /**
     * Foundation for sorting
     *
     * @param o item to compare to
     * @return compareTo indicator int
     */
    @Override
    public int compareTo(SortableItem<T> o) {
        return this.sortableKey.compareToIgnoreCase(o.sortableKey);
    }
}
