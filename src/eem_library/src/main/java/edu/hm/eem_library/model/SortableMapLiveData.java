package edu.hm.eem_library.model;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class SortableMapLiveData<S extends Comparable<? super S>, T> extends MutableLiveData<ArrayList<SortableItem<S,T>>> {
    final SortedMap<S, SortableItem<S,T>> backingMap;
    final boolean notificationNeeded;

    public SortableMapLiveData(@Nullable Set<SortableItem<S, T>> set, boolean notificationNeeded) {
        this.notificationNeeded = notificationNeeded;
        backingMap = new TreeMap<>();
        if(set != null) {
            refreshData(set);
        }
        if(notificationNeeded) notifyObservers(false);
    }

    void refreshData(@NonNull Set<SortableItem<S,T>> set){
        backingMap.clear();
        for (SortableItem<S, T> item : set) {
            backingMap.put(item.sortableKey, item);
        }
    }

    public boolean add(S sortableKey, T item){
        boolean ret = !backingMap.containsKey(sortableKey);
        if (ret) {
            put(sortableKey,item);
            if(notificationNeeded) notifyObservers(false);
        }
        return ret;
    }

    private void put(S sortableKey, T item){
        backingMap.put(sortableKey, new SortableItem<>(sortableKey, item));
    }

    public SortableItem<S,T> remove(S sortableKey){
        SortableItem<S,T> removed = backingMap.remove(sortableKey);
        if(notificationNeeded) notifyObservers(false);
        return removed;
    }

    public boolean contains(S sortableKey){
        return backingMap.containsKey(sortableKey);
    }

    /** Notify observers on a livedata change
     *
     * @param post if true, notify Observers from background thread such as a listener
     */
    void notifyObservers(boolean post){
        ArrayList<SortableItem<S,T>> list = new ArrayList<>(backingMap.values());
        if(post)
            postValue(list);
        else
            setValue(list);
    }
}
