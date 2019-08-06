package edu.hm.eem_library.model;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;

public class SelectableSortableItemLiveData<V,T extends SelectableSortableItem<V>> extends SortableItemLiveData<V, T> {
    private int selectionCounter;

    SelectableSortableItemLiveData(boolean notificationNeeded) {
        super(notificationNeeded);
    }

    public void toggleSelected(int index) {
        selectionCounter += (getValue().get(index).selected^=true) ? 1 : -1;
        notifyObserversMeta();
    }

    public void setSelected(String name, boolean notify){
        backingMap.get(name).selected = true;
        selectionCounter++;
        if(notify)
            notifyObserversMeta();
    }

    public void setSelected(){
        for(T item : getValue()){
            item.selected = true;
            selectionCounter++;
        }
        notifyObserversMeta();
    }

    /** Get a selected item in the map.
     *
     * @return The first item in the list, which is selected. If none is selected, returns null.
     */
    @Nullable
    public V getSelected(){
        V ret = null;
        for(SelectableSortableItem<V> item : backingMap.values()){
            if(item.selected){
                ret = item.item;
                break;
            }
        }
        return ret;
    }

    /** Notfify Observers, but only the selection has changed, so the Arraylist does not need to be
     * rebuilt.
     */
    void notifyObserversMeta(){
        postValue(getValue());
    }

    public int getSelectionCount(){
        return selectionCounter;
    }

    public void clearSelection(String name){
        backingMap.get(name).selected = false;
        selectionCounter--;
        notifyObserversMeta();
    }

    public void clearSelection() {
        for(SelectableSortableItem<V> item : backingMap.values())
            item.selected = false;
        selectionCounter = 0;
        notifyObserversMeta();
    }

    @Nullable
    public ArrayList<T> removeSelected(){
        ArrayList<T> removed = null;
        if(selectionCounter>0) {
            removed = new ArrayList<>(selectionCounter);
            LinkedList<String> removedKeyList = new LinkedList<>();
            for (SelectableSortableItem<V> item : backingMap.values()){
                if(item.selected) {
                    removedKeyList.add(item.getSortableKey());
                }
            }
            for (String s : removedKeyList){
                removed.add(backingMap.remove(s));
            }
            selectionCounter = 0;
            if(notificationNeeded) notifyObservers(false);
        }
        return removed;
    }

    @Override
    public T remove(String sortableKey, boolean post) {
        selectionCounter--;
        return super.remove(sortableKey, post);
    }

    @Override
    void notifyObservers(boolean post) {
        super.notifyObservers(post);
    }

    @Override
    public void clean(boolean post) {
        selectionCounter = 0;
        super.clean(post);
    }
}
