package edu.hm.eem_host.model;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.os.FileObserver;

public class ExamListHost extends SelectableItemHost {

    private ExamListLiveData livedata;

    public ExamListHost(Application application) {
        super(application);
        this.livedata = new ExamListLiveData();
    }

    public class ExamListLiveData extends MutableLiveData<ExamList> {
        private final int FILEOBSERVERMASK = FileObserver.DELETE | FileObserver.CREATE;
        private final FileObserver fileObserver;

        ExamListLiveData() {
            super();
            fileObserver = new FileObserver(examDir.getPath(), FILEOBSERVERMASK) {
                @Override
                public void onEvent(int event, String path) {
                    // Filelist has been modified; Update self
                    postValue(new ExamList(examDir));
                }
            };
            fileObserver.startWatching();
            setValue(new ExamList(examDir));
        }

        public void add(SelectableItem<Nameable> item){
            getValue().add(item);
        }

        public void removeSelected(){
            getValue().removeSelected();
        }

        @Override
        protected void onActive() {
            fileObserver.startWatching();
        }

        @Override
        protected void onInactive() {

        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ExamListLiveData getLivedata() {
        return livedata;
    }
}
