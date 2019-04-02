package edu.hm.eem_host.model;

public class SelectableItem<T extends Nameable> implements Comparable<SelectableItem<T>>{
    public boolean selected;
    public T dataItem;

    SelectableItem(T item) {
        this.dataItem = item;
        this.selected = false;
    }

    @Override
    public int compareTo(SelectableItem<T> o){
        return dataItem.getName().compareTo(o.dataItem.getName());
    }
}
