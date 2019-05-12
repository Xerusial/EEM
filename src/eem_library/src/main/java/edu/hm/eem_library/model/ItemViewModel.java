package edu.hm.eem_library.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

public abstract class ItemViewModel<T extends MutableLiveData> extends AndroidViewModel {
    protected T livedata;

    protected ItemViewModel(@NonNull Application application) {
        super(application);
    }

    public T getLivedata(){
        return livedata;
    }
}
