package edu.hm.eem_library.model;

import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class SortableMapLiveData<K extends Comparable<? super K>, V, T extends SortableItem<K,V>> extends MutableLiveData<ArrayList<T>> {
    final SortedMap<K, T> backingMap;
    final boolean notificationNeeded;

    SortableMapLiveData(@Nullable Set<T> set, boolean notificationNeeded) {
        this.notificationNeeded = notificationNeeded;
        backingMap = new TreeMap<>();
        if(set != null) {
            refreshData(set);
        }
        if(notificationNeeded) notifyObservers(false);
    }

    void refreshData(@NonNull Set<T> set){
        backingMap.clear();
        for (T item : set) {
            backingMap.put(item.sortableKey, item);
        }
    }

    public boolean add(K sortableKey, T item, boolean post){
        boolean ret = !backingMap.containsKey(sortableKey);
        if (ret) {
            put(sortableKey,item);
            if(notificationNeeded) notifyObservers(post);
        }
        return ret;
    }

    private void put(K sortableKey, T item){
        backingMap.put(sortableKey, item);
    }

    public T remove(K sortableKey, boolean post){
        T removed = backingMap.remove(sortableKey);
        if(notificationNeeded) notifyObservers(post);
        return removed;
    }

    public void clean(boolean post){
        backingMap.clear();
        notifyObservers(post);
    }

    public boolean isEmpty(){
        return backingMap.isEmpty();
    }

    public boolean contains(K sortableKey){
        return backingMap.containsKey(sortableKey);
    }

    /** Notify observers on a livedata change
     *
     * @param post if true, notify Observers from background thread such as a listener
     */
    void notifyObservers(boolean post){
        ArrayList<T> list = new ArrayList<>(backingMap.values());
        if(post)
            postValue(list);
        else
            setValue(list);
    }
}
