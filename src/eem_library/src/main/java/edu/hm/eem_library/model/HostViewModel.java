package edu.hm.eem_library.model;

import android.app.Application;
import android.net.nsd.NsdServiceInfo;
import androidx.annotation.NonNull;

public class HostViewModel extends ItemViewModel<SelectableSortableMapLiveData<String, NsdServiceInfo>> {
    public HostViewModel(@NonNull Application application) {
        super(application);
        this.livedata = new SelectableSortableMapLiveData<>(null, true);
    }

    public NsdServiceInfo get(int index){
        return livedata.getValue().get(index).item;
    }
}
