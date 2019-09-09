package edu.hm.eem_library.model;

import android.app.Application;

import java.io.File;
import java.util.Objects;

public abstract class ExamDocumentItemViewModel<T extends StudentExam> extends FilebackedItemViewModel<ExamDocumentItemViewModel.ExamDocumentItemLiveData> {
    ExamFactory factory;
    private T current;
    private String currentName;

    ExamDocumentItemViewModel(Application application) {
        super(application);
        this.livedata = new ExamDocumentItemLiveData(true);
    }

    public void openExam(String name) {
        current = (T) factory.get(new File(examDir.getPath() + File.separator + name));
        currentName = name;
        this.livedata.refreshData(Objects.requireNonNull(current).toLiveDataSet(getApplication()), true);
    }

    public void closeExam() {
        current.documentsFromLivedata(livedata.backingMap.values());
        factory.writeExamToFile(current, new File(examDir + File.separator + currentName));
    }

    public T getCurrent() {
        return current;
    }

    public class ExamDocumentItemLiveData extends SelectableSortableItemLiveData<ExamDocument, ThumbnailedExamDocument> {
        ExamDocumentItemLiveData(boolean notificationNeeded) {
            super(notificationNeeded);
        }

        @Override
        public boolean add(ThumbnailedExamDocument container, boolean post) {
            while (!super.add(container, post)) {
                container.incrKey();
            }
            return true;
        }

        void setRejected(int index, ThumbnailedExamDocument.RejectionReason reason) {
            Objects.requireNonNull(getValue()).get(index).reason = reason;
            toggleSelected(index);
        }
    }
}

