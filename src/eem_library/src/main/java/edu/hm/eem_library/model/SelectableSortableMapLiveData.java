package edu.hm.eem_library.model;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class SelectableSortableMapLiveData<K extends Comparable<? super K>,V,T extends SortableItem<K , V>> extends SortableMapLiveData<K, V, T> {
    private boolean[] selection;
    private int selectionCounter;

    SelectableSortableMapLiveData(@Nullable Set<T> set, boolean notificationNeeded) {
        super(set, notificationNeeded);
    }

    public void toggleSelected(int index) {
        selectionCounter += (selection[index]^=true) ? 1 : -1;
        notifyObserversMeta();
    }

    /** Get a selected item in the map.
     *
     * @return The first item in the list, which is selected. If none is selected, returns null.
     */
    @Nullable
    public T getSelected(){
        T ret = null;
        for(int i = 0; i<selection.length; i++){
            if(selection[i]){
                ret = getValue().get(i);
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

    public boolean isSelected(int index){
        return selection[index];
    }

    public int getSelectionCount(){
        return selectionCounter;
    }

    public void clearSelection() {
        Arrays.fill(selection,false);
        selectionCounter = 0;
    }

    @Nullable
    public ArrayList<T> removeSelected(){
        ArrayList<T> removed = null;
        if(selectionCounter>0) {
            removed = new ArrayList<>(selectionCounter);
            for(int i = 0; i<selection.length; i++){
                if(selection[i]){
                    removed.add(backingMap.remove(getValue().get(i).sortableKey));
                }
            }
            clearSelection();
            if(notificationNeeded) notifyObservers(false);
        }
        return removed;
    }

    @Override
    void notifyObservers(boolean post) {
        this.selection = new boolean[backingMap.size()];
        this.selectionCounter = 0;
        super.notifyObservers(post);
    }


}
