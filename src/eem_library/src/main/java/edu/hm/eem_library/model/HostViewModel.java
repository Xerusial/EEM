package edu.hm.eem_library.model;

import android.app.Application;
import androidx.annotation.NonNull;

import edu.hm.eem_library.net.NsdService;

public class HostViewModel extends ItemViewModel<SelectableSortableMapLiveData<NsdService, SelectableSortableItem<NsdService>>> {
    public HostViewModel(@NonNull Application application) {
        super(application);
        this.livedata = new SelectableSortableMapLiveData<>(null, true);
    }

    public NsdService get(int index){
        return livedata.getValue().get(index).item;
    }
}
