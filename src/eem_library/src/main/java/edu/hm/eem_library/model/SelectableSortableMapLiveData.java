package edu.hm.eem_library.model;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Set;

public class SelectableSortableMapLiveData<V,T extends SelectableSortableItem<V>> extends SortableMapLiveData<V, T> {
    private int selectionCounter;

    SelectableSortableMapLiveData(@Nullable Set<T> set, boolean notificationNeeded) {
        super(set, notificationNeeded);
    }

    public void toggleSelected(int index) {
        selectionCounter += (getValue().get(index).selected^=true) ? 1 : -1;
        notifyObserversMeta();
    }

    public void toggleSelected(String name){
        selectionCounter += (backingMap.get(name).selected^= true) ? 1 : -1;
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
    private void notifyObserversMeta(){
        postValue(getValue());
    }

    public int getSelectionCount(){
        return selectionCounter;
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
            for (SelectableSortableItem<V> item : backingMap.values()){
                if(item.selected) {
                    removed.add(backingMap.remove(item.getSortableKey()));
                }
            }
            selectionCounter = 0;
            if(notificationNeeded) notifyObservers(false);
        }
        return removed;
    }

    @Override
    void notifyObservers(boolean post) {
        super.notifyObservers(post);
    }


}
