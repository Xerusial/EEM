package edu.hm.eem_library.model;

public class SortableItem<T> implements Comparable<SortableItem<T>>{
    private String sortableKey;
    public final T item;

    SortableItem(String sortableKey, T item) {
        this.sortableKey = sortableKey;
        this.item = item;
    }

    public String getSortableKey() {
        return sortableKey;
    }

    void incrKey(){
        String[] parts = sortableKey.split(" ");
        int preEndIndex = parts.length-1;
        int num;
        try {
            num = Integer.parseInt(parts[preEndIndex]);
            num++;
        } catch (NumberFormatException e){
            num = 1;
            preEndIndex = parts.length;
        }
        sortableKey = "";
        StringBuilder builder = new StringBuilder(parts[0] + ' ');
        for(int i = 1; i<preEndIndex; i++){
            builder.append(parts[i]);
            builder.append(' ');
        }
        builder.append(num);
        sortableKey = builder.toString();
    }

    @Override
    public int compareTo(SortableItem<T> o) {
        return this.sortableKey.compareToIgnoreCase(o.sortableKey);
    }
}
