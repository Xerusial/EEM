package edu.hm.eem_library.model;

import android.app.Application;

import java.util.Objects;

import edu.hm.eem_library.net.ClientItem;

/**
 * Subclass of {@link ItemViewModel}. Check out {@link ItemViewModel} for full hierarchy
 */
public class ClientItemViewModel extends ItemViewModel<ClientItemViewModel.ClientItemLiveData> {
    public ClientItemViewModel(Application application) {
        super(application);
        this.livedata = new ClientItemLiveData();
    }

    /**
     * This livedata iis backing a {@link androidx.recyclerview.widget.RecyclerView} containing
     * {@link ClientItem} (Students) which are contained in a sorted list
     */
    public class ClientItemLiveData extends SelectableSortableItemLiveData<ClientItem, SelectableSortableItem<ClientItem>> {

        ClientItemLiveData() {
            super(true);
        }

        /**
         * Send the lighthouse signal to the student
         *
         * @param index which student
         */
        public void lighthouse(int index) {
            Objects.requireNonNull(getValue()).get(index).item.lighthoused ^= true;
            notifyObserversMeta();
        }

        /**
         * Delete all disconnected students from list
         *
         * @param post if this should be posted to the UI thread
         */
        public void clearDisconnected(boolean post) {
            for (int i = Objects.requireNonNull(getValue()).size() - 1; i >= 0; i--) {
                SelectableSortableItem<ClientItem> item = getValue().get(i);
                if (item.item.disconnected) {
                    backingMap.remove(item.getSortableKey());
                    selectionCounter--;
                }
            }
            notifyObservers(post);
        }
    }
}


