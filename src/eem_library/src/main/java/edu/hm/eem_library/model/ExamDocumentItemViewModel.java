package edu.hm.eem_library.model;

import android.app.Application;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.Set;

public abstract class ExamDocumentItemViewModel<T extends StudentExam> extends FilebackedItemViewModel<ExamDocumentItemViewModel.ExamDocumentItemLiveData> {
    private T current;
    private String currentName;
    ExamFactory factory;

    ExamDocumentItemViewModel(Application application) {
        super(application);
    }

    public class ExamDocumentItemLiveData extends SelectableSortableItemLiveData<ExamDocument, ThumbnailedExamDocument> {
        ExamDocumentItemLiveData(@Nullable Set<ThumbnailedExamDocument> set, boolean notificationNeeded) {
            super(set, notificationNeeded);
        }

        @Override
        public boolean add(ThumbnailedExamDocument container, boolean post) {
            while(!super.add(container, post)){
                container.incrKey();
            }
            return true;
        }

        void setRejected(int index, ThumbnailedExamDocument.RejectionReason reason){
            getValue().get(index).reason = reason;
            toggleSelected(index);
        }
    }

    public void openExam(String name) {
        current = (T) factory.get(new File(examDir.getPath() + File.separator + name));
        currentName = name;
        this.livedata = new ExamDocumentItemLiveData(current.toLiveDataSet(getApplication()), true);
    }

    public void closeExam(){
        current.documentsFromLivedata(livedata.backingMap.values());
        factory.writeExamToFile(current, new File(examDir + File.separator + currentName));
    }

    public T getCurrent() {
        return current;
    }
}

