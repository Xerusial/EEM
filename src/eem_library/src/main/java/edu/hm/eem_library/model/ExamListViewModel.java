package edu.hm.eem_library.model;

import android.app.Application;
import android.os.FileObserver;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class ExamListViewModel extends FilebackedItemViewModel<ExamListViewModel.ExamListLiveData> {

    public ExamListViewModel(Application application) {
        super(application);
        this.livedata = new ExamListLiveData();
    }

    public class ExamListLiveData extends SelectableSortableMapLiveData<String, File> {
        private final int FILEOBSERVERMASK = FileObserver.DELETE | FileObserver.CREATE;
        private final FileObserver fileObserver;

        ExamListLiveData() {
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

        private Set<SortableItem<String,File>> getDir(){
            Set<SortableItem<String,File>> ret = new TreeSet<>();
            for(File f: examDir.listFiles()){
                ret.add(new SortableItem<>(f.getName(), f));
            }
            return ret;
        }

        @Nullable
        @Override
        public ArrayList<SortableItem<String, File>> removeSelected() {
            ArrayList<SortableItem<String, File>> ret = super.removeSelected();
            for (SortableItem<String, File> s: ret){
                //noinspection ResultOfMethodCallIgnored
                s.item.delete();
            }
            return ret;
        }

        @Override
        public SortableItem<String, File> remove(String sortableKey, boolean post) {
            SortableItem<String, File> ret = super.remove(sortableKey, post);
            //noinspection ResultOfMethodCallIgnored
            ret.item.delete();
            return ret;
        }
    }
}
