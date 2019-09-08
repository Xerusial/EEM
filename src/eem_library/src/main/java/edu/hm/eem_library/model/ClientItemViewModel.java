package edu.hm.eem_library.model;

import android.app.Application;

import java.util.function.Predicate;

import edu.hm.eem_library.net.ClientItem;

public class ClientItemViewModel extends ItemViewModel<ClientItemViewModel.ClientItemLiveData> {
    public ClientItemViewModel(Application application) {
        super(application);
        this.livedata = new ClientItemLiveData(true);
    }

    public class ClientItemLiveData extends SelectableSortableItemLiveData<ClientItem, SelectableSortableItem<ClientItem>> {

        ClientItemLiveData(boolean notificationNeeded) {
            super(notificationNeeded);
        }

        public void lighthouse(int index){
            getValue().get(index).item.lighthoused ^= true;
            notifyObserversMeta();
        }

        public void disconnected(String name){
            backingMap.get(name).item.disconnected = true;
            backingMap.get(name).selected = false;
            notifyObserversMeta();
        }

        public void clearDisconnected(boolean post){
            for(int i = getValue().size()-1; i>=0; i--){
                SelectableSortableItem<ClientItem> item = getValue().get(i);
                if(item.item.disconnected){
                    backingMap.remove(item.getSortableKey());
                    selectionCounter--;
                }
            }
            notifyObservers(post);
        }

        public void incrCountNotificationDrawer(String name){
            backingMap.get(name).item.countNotificationDrawer++;
            notifyObserversMeta();
        }
    }
}


