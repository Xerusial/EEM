package edu.hm.eem_library.model;

import android.app.Application;
import android.os.FileObserver;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Subclass of {@link ItemViewModel}. Check out {@link ItemViewModel} for full hierarchy
 */
public class ExamItemViewModel extends FilebackedItemViewModel<ExamItemViewModel.ExamItemLiveData> {

    public ExamItemViewModel(Application application) {
        super(application);
        this.livedata = new ExamItemLiveData();
    }

    /**
     * {@link androidx.lifecycle.LiveData} backing a {@link androidx.recyclerview.widget.RecyclerView}
     * containing exam yaml file names
     */
    public class ExamItemLiveData extends SelectableSortableItemLiveData<File, SelectableSortableItem<File>> {
        private final int FILEOBSERVERMASK = FileObserver.DELETE | FileObserver.CREATE;
        //keep reference of Fileobserver because otherwise it will be GCed
        @SuppressWarnings("FieldCanBeLocal")
        private final FileObserver fileObserver;

        /**
         * Initialize file observer to keep {@link androidx.recyclerview.widget.RecyclerView} in
         * sync with the storage directory
         */
        ExamItemLiveData() {
            super(false);
            // Filelist has been modified; Update self
            //noinspection deprecation
            fileObserver= new FileObserver(examDir.getPath(), FILEOBSERVERMASK) {
                @Override
                public void onEvent(int event, String path) {
                    // Filelist has been modified; Update self
                    refreshData(getDir(), true);
                }
            };
            fileObserver.startWatching();
            refreshData(getDir(), false);
        }

        /**
         * Create a set from storage directories contents: "The list
         *
         * @return a list with unique identifiers
         */
        private Set<SelectableSortableItem<File>> getDir() {
            Set<SelectableSortableItem<File>> ret = new TreeSet<>();
            for (File f : Objects.requireNonNull(examDir.listFiles())) {
                ret.add(new SelectableSortableItem<>(f.getName(), f));
            }
            return ret;
        }

        /**
         * Remove selected entries from liveData
         *
         * @return Array of deleted files
         */
        @Nullable
        @Override
        public ArrayList<SelectableSortableItem<File>> removeSelected() {
            ArrayList<SelectableSortableItem<File>> ret = super.removeSelected();
            for (SelectableSortableItem<File> s : Objects.requireNonNull(ret)) {
                //noinspection ResultOfMethodCallIgnored
                s.item.delete();
            }
            return ret;
        }

        /**
         * Remove certain YAML exam file
         *
         * @param sortableKey unique name of exam
         * @param post        post to UI thread
         * @return Removed exam file
         */
        @Override
        public SelectableSortableItem<File> remove(String sortableKey, boolean post) {
            SelectableSortableItem<File> ret = super.remove(sortableKey, post);
            //noinspection ResultOfMethodCallIgnored
            ret.item.delete();
            return ret;
        }


    }
}
