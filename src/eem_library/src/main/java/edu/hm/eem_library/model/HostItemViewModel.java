package edu.hm.eem_library.model;

import android.app.Application;

import androidx.annotation.NonNull;

import java.util.Objects;

import edu.hm.eem_library.net.NsdService;

public class HostItemViewModel extends ItemViewModel<SelectableSortableItemLiveData<NsdService, SelectableSortableItem<NsdService>>> {
    public HostItemViewModel(@NonNull Application application) {
        super(application);
        this.livedata = new SelectableSortableItemLiveData<>(true);
    }

    public NsdService get(int index) {
        return Objects.requireNonNull(livedata.getValue()).get(index).item;
    }
}
