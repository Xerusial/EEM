package edu.hm.eem_library.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Subclass of {@link SortableItemLiveData}. Check out {@link SortableItemLiveData} for full hierarchy
 *
 * @param <V> item for Sortableitem container
 * @param <T> Type of SelectableSortableItem
 */
public class SelectableSortableItemLiveData<V, T extends SelectableSortableItem<V>> extends SortableItemLiveData<V, T> {
    public int selectionCounter;

    /**
     * Constructor
     *
     * @param notificationNeeded whether Recyclerview's observers need to be notified when updating data
     */
    SelectableSortableItemLiveData(boolean notificationNeeded) {
        super(notificationNeeded);
    }

    /**
     * Toggle selected property of an item
     *
     * @param index item index
     */
    public void toggleSelected(int index) {
        selectionCounter += (Objects.requireNonNull(getValue()).get(index).selected ^= true) ? 1 : -1;
        notifyObserversMeta();
    }

    /**
     * Set selected property of an item
     *
     * @param name   unique name of the item
     * @param notify whether to notify the attached observers
     */
    public void setSelected(String name, boolean notify) {
        Objects.requireNonNull(backingMap.get(name)).selected = true;
        selectionCounter++;
        if (notify)
            notifyObserversMeta();
    }

    /**
     * Set selected property of all contained items
     */
    public void setSelected() {
        for (T item : Objects.requireNonNull(getValue())) {
            if (!item.selected) {
                item.selected = true;
                selectionCounter++;
            }
        }
        notifyObserversMeta();
    }

    /**
     * Get a selected item in the map.
     *
     * @return The first item in the list, which is selected. If none is selected, returns null.
     */
    @Nullable
    public V getSelected() {
        V ret = null;
        for (SelectableSortableItem<V> item : backingMap.values()) {
            if (item.selected) {
                ret = item.item;
                break;
            }
        }
        return ret;
    }

    /**
     * Notify Observers, but only the selection has changed, so the Arraylist does not need to be
     * rebuilt.
     */
    public void notifyObserversMeta() {
        postValue(getValue());
    }

    /**
     * Get the number of selected items
     *
     * @return number
     */
    public int getSelectionCount() {
        return selectionCounter;
    }

    /**
     * Deselect an item
     *
     * @param name unique name of the item
     */
    public void clearSelection(String name) {
        Objects.requireNonNull(backingMap.get(name)).selected = false;
        selectionCounter--;
        notifyObserversMeta();
    }

    /**
     * Clear selection of all items
     */
    public void clearSelection() {
        for (SelectableSortableItem<V> item : backingMap.values())
            item.selected = false;
        selectionCounter = 0;
        notifyObserversMeta();
    }

    /**
     * Remove selected items
     *
     * @return An arraylist of removed items
     */
    @Nullable
    public ArrayList<T> removeSelected() {
        ArrayList<T> removed = null;
        if (selectionCounter > 0) {
            removed = new ArrayList<>(selectionCounter);
            LinkedList<String> removedKeyList = new LinkedList<>();
            for (SelectableSortableItem<V> item : backingMap.values()) {
                if (item.selected) {
                    removedKeyList.add(item.getSortableKey());
                }
            }
            for (String s : removedKeyList) {
                removed.add(backingMap.remove(s));
            }
            selectionCounter = 0;
            if (notificationNeeded) notifyObservers(false);
        }
        return removed;
    }

    /**
     * Remove an item by name
     *
     * @param sortableKey the unique name of the item
     * @param post        post to UI thread
     * @return th removed item
     */
    @Override
    public T remove(String sortableKey, boolean post) {
        T item = super.remove(sortableKey, post);
        if (item.selected) selectionCounter--;
        return item;
    }

    /**
     * Clear entire list
     *
     * @param post post to UI thread
     */
    @Override
    public void clean(boolean post) {
        selectionCounter = 0;
        super.clean(post);
    }
}
