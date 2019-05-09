package edu.hm.eem_library.model;

import android.app.Application;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.NonNull;

public class HostViewModel extends ItemViewModel<SelectableSortableMapLiveData<String, NsdServiceInfo>> {
    public HostViewModel(@NonNull Application application) {
        super(application);
        this.livedata = new SelectableSortableMapLiveData<>(null);
    }

    public NsdServiceInfo get(int index){
        return livedata.getValue()[index].item;
    }
}
