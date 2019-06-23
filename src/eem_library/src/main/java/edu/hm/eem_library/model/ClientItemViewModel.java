package edu.hm.eem_library.model;

import android.app.Application;

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

        public void incrCountNotificationDrawer(String name){
            backingMap.get(name).item.countNotificationDrawer++;
            notifyObserversMeta();
        }
    }
}


