package edu.hm.eem_library.model;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class SortableMapLiveData<S extends Comparable<? super S>, T> extends MutableLiveData<SortableItem<S,T>[]> {
    final SortedMap<S, SortableItem<S,T>> backingMap;

    public SortableMapLiveData(@Nullable Set<SortableItem<S, T>> set) {
        backingMap = new TreeMap<>();
        if(set != null) {
            refreshData(set);
        }
        notifyObservers();
    }

    void refreshData(@NonNull Set<SortableItem<S,T>> set){
        for (SortableItem<S, T> item : set) {
            backingMap.put(item.sortableKey, item);
        }
    }

    public boolean add(S sortableKey, T item){
        boolean ret = !backingMap.containsKey(sortableKey);
        if (ret) {
            put(sortableKey,item);
            notifyObservers();
        }
        return ret;
    }

    private void put(S sortableKey, T item){
        backingMap.put(sortableKey, new SortableItem<>(sortableKey, item));
    }

    public SortableItem<S,T> remove(S sortableKey){
        SortableItem<S,T> removed = backingMap.remove(sortableKey);
        notifyObservers();
        return removed;
    }

    public boolean contains(S sortableKey){
        return backingMap.containsKey(sortableKey);
    }

    void notifyObservers(){
        setValue((SortableItem<S,T>[])backingMap.values().toArray());
    }
}
