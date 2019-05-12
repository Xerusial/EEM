package edu.hm.eem_library.model;

import android.app.Application;

import edu.hm.eem_library.net.ClientDevice;

public class DeviceViewModel extends ItemViewModel<SortableMapLiveData<String, ClientDevice>> {
    public DeviceViewModel(Application application) {
        super(application);
        this.livedata = new SortableMapLiveData<>(null, true);
    }
}


