package edu.hm.eem_library.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The base class for all {@link androidx.lifecycle.LiveData} in this project. LiveData are
 * livecycle aware objects, to which an observer of a livecycle owner can subscribe. In this project,
 * livedata is used to inform {@link androidx.recyclerview.widget.RecyclerView}s of changes of their
 * content. All observers are notified with the use of the setValue method. The inheritance tree of
 * LiveData with their respective items used in this project is shown in the following.
 *
 *                        .----------------------.
 *                        | SortableItemLiveData |
 *                        |----------------------|
 *                        | SortableItem         |
 *                        '----------------------'
 *                                    ^
 *                                    |
 *                                    |
 *                   .--------------------------------.
 *                   | SelectableSortableItemLiveData |
 *                .->|--------------------------------|<-.
 *                |  | SelectableSortableItem         |  |
 *                |  '--------------------------------'  |
 *                |                   ^                  |
 *  .---------------------------.     |  .------------------------------.
 *  | ExamDocumentItemLiveData  |     |  |       ExamItemLiveData       |
 *  |---------------------------|     |  |------------------------------|
 *  | ThumbnailedExamDocument   |     |  | SelectableSortableItem<File> |
 *  '---------------------------'     |  '------------------------------'
 *                                    |
 *                 .------------------------------------.
 *                 |         ClientItemLiveData         |
 *                 |------------------------------------|
 *                 | SelectableSortableItem<ClientItem> |
 *                 '------------------------------------'
 *
 * @param <V> The real item contained in the backingmap of this livedata
 * @param <T> The SortableItem container to hold the item
 */
public class SortableItemLiveData<V, T extends SortableItem<V>> extends MutableLiveData<ArrayList<T>> {
    final SortedMap<String, T> backingMap;
    final boolean notificationNeeded;

    SortableItemLiveData(boolean notificationNeeded) {
        this.notificationNeeded = notificationNeeded;
        backingMap = new TreeMap<>();
        if (notificationNeeded) notifyObservers(false);
    }

    void refreshData(@NonNull Set<T> set, boolean post) {
        backingMap.clear();
        for (T item : set) {
            backingMap.put(item.getSortableKey(), item);
        }
        notifyObservers(post);
    }

    public boolean add(T item, boolean post) {
        boolean ret = !backingMap.containsKey(item.getSortableKey());
        if (ret) {
            put(item);
            if (notificationNeeded) notifyObservers(post);
        }
        return ret;
    }

    private void put(T item) {
        backingMap.put(item.getSortableKey(), item);
    }

    public T remove(String sortableKey, boolean post) {
        T removed = backingMap.remove(sortableKey);
        if (notificationNeeded) notifyObservers(post);
        return removed;
    }

    public void clean(boolean post) {
        backingMap.clear();
        notifyObservers(post);
    }

    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    public boolean contains(String sortableKey) {
        return backingMap.containsKey(sortableKey);
    }

    /**
     * Notify observers on a livedata change
     *
     * @param post if true, notify Observers from background thread such as a listener
     */
    void notifyObservers(boolean post) {
        ArrayList<T> list = new ArrayList<>(backingMap.values());
        if (post)
            postValue(list);
        else
            setValue(list);
    }
}
