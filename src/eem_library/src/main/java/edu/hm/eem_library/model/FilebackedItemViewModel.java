package edu.hm.eem_library.model;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;

import java.io.File;

import edu.hm.eem_library.net.FilePacket;

abstract class FilebackedItemViewModel<T extends MutableLiveData> extends ItemViewModel<T> {
    File examDir;

    FilebackedItemViewModel(Application application){
        super(application);
        String internalStoragePath = application.getFilesDir().getPath() + File.separator + FilePacket.EXAMDIR;
        examDir = new File(internalStoragePath);
        if(!examDir.isDirectory())
            //noinspection ResultOfMethodCallIgnored
            examDir.mkdir();
    }
}
