package edu.hm.eem_library.model;

import android.app.Application;

public class StringMapViewModel<T> extends ItemViewModel<SortableMapLiveData<String,T>> {

    public StringMapViewModel(Application application) {
        super(application);
        this.livedata = new SortableMapLiveData<>(null);
    }
}
