package edu.hm.eem_library.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

public abstract class ItemViewModel<T extends MutableLiveData> extends AndroidViewModel {
    protected T livedata;

    protected ItemViewModel(@NonNull Application application) {
        super(application);
    }

    public T getLivedata(){
        return livedata;
    }
}
