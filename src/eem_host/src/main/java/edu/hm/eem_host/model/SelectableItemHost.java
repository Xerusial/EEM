package edu.hm.eem_host.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;

import java.io.File;

public abstract class SelectableItemHost extends AndroidViewModel {
    File examDir;

    SelectableItemHost(Application application){
        super(application);
        String internalStoragePath = application.getFilesDir().getPath() + File.separator + "exams";
        examDir = new File(internalStoragePath);
        if(!examDir.isDirectory())
            //noinspection ResultOfMethodCallIgnored
            examDir.mkdir();
    }

    public abstract <T extends Nameable> MutableLiveData<SelectableItemList<T>> getLivedata();
}
