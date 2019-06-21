package edu.hm.eem_library.model;

import android.app.Application;

import edu.hm.eem_library.net.ClientItem;

public class ClientItemViewModel extends ItemViewModel<SelectableSortableItemLiveData<ClientItem, SelectableSortableItem<ClientItem>>> {
    public ClientItemViewModel(Application application) {
        super(application);
        this.livedata = new SelectableSortableItemLiveData<>(null, true);
    }


}


