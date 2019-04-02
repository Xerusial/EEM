package edu.hm.eem_host.model;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedList;

public class SelectableItemList<T extends Nameable> extends LinkedList<SelectableItem<T>> {

    SelectableItemList() {
    }

    SelectableItemList(@NonNull LinkedList<T> list) {
        for(T item : list) add(new SelectableItem<>(item));
    }

    void removeSelected(){
        for(SelectableItem item : this){
            if(item.selected){
                remove(item);
            }
        }
    }

    public void clearSelection() {
        for (SelectableItem<T> item : this)
            item.selected = false;
    }

    @Override
    public boolean add(SelectableItem<T> item) {
        int index = Collections.binarySearch(this, item);
        if (index < 0) index = ~index;
        add(index, item);
        return true; //specified by {@link Collection}
    }

    LinkedList<T> getRawList(){
        LinkedList<T> list = new LinkedList<>();
        for(SelectableItem<T> item : this)
            list.add(item.dataItem);
        return list;
    }
}
