package edu.hm.eem_library.model;

import android.app.Application;
import android.os.FileObserver;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class ExamItemViewModel extends FilebackedItemViewModel<ExamItemViewModel.ExamItemLiveData> {

    public ExamItemViewModel(Application application) {
        super(application);
        this.livedata = new ExamItemLiveData();
    }

    public class ExamItemLiveData extends SelectableSortableItemLiveData<File, SelectableSortableItem<File>> {
        private final int FILEOBSERVERMASK = FileObserver.DELETE | FileObserver.CREATE;
        private final FileObserver fileObserver;

        ExamItemLiveData() {
            super(null, false);
            fileObserver = new FileObserver(examDir.getPath(), FILEOBSERVERMASK) {
                @Override
                public void onEvent(int event, String path) {
                    // Filelist has been modified; Update self
                    refreshData(getDir());
                    notifyObservers(true);
                }
            };
            fileObserver.startWatching();
            refreshData(getDir());
            notifyObservers(false);
        }

        private Set<SelectableSortableItem<File>> getDir(){
            Set<SelectableSortableItem<File>> ret = new TreeSet<>();
            for(File f: examDir.listFiles()){
                ret.add(new SelectableSortableItem<>(f.getName(), f));
            }
            return ret;
        }

        @Nullable
        @Override
        public ArrayList<SelectableSortableItem<File>> removeSelected() {
            ArrayList<SelectableSortableItem<File>> ret = super.removeSelected();
            for (SelectableSortableItem<File> s: ret){
                //noinspection ResultOfMethodCallIgnored
                s.item.delete();
            }
            return ret;
        }

        @Override
        public SelectableSortableItem<File> remove(String sortableKey, boolean post) {
            SelectableSortableItem<File> ret = super.remove(sortableKey, post);
            //noinspection ResultOfMethodCallIgnored
            ret.item.delete();
            return ret;
        }
    }
}
