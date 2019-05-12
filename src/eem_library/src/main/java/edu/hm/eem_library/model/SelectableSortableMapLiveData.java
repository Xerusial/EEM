package edu.hm.eem_library.model;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class SelectableSortableMapLiveData<S extends Comparable<? super S>, T> extends SortableMapLiveData<S, T> {
    private boolean[] selection;
    private int selectionCounter;

    SelectableSortableMapLiveData(@Nullable Set<SortableItem<S, T>> set, boolean notificationNeeded) {
        super(set, notificationNeeded);
    }

    public void toggleSelected(int index) {
        selectionCounter += (selection[index]^=true) ? 1 : -1;
    }

    /** Get a selected item in the map.
     *
     * @return The first item in the list, which is selected. If none is selected, returns null.
     */
    @Nullable
    public SortableItem<S,T> getSelected(){
        SortableItem<S,T> ret = null;
        for(int i = 0; i<selection.length; i++){
            if(selection[i]){
                ret = getValue().get(i);
                break;
            }
        }
        return ret;
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
    public ArrayList<SortableItem<S,T>> removeSelected(){
        ArrayList<SortableItem<S,T>> removed = null;
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
