package edu.hm.eem_library.model;

import android.app.Application;

import java.net.Socket;

public class DeviceViewModel extends ItemViewModel<SortableMapLiveData<Socket, SortableItem<Socket>>> {
    public DeviceViewModel(Application application) {
        super(application);
        this.livedata = new SortableMapLiveData<>(null, true);
    }


}


