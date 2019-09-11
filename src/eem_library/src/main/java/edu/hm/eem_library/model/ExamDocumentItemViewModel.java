package edu.hm.eem_library.model;

import android.app.Application;

import java.io.File;
import java.util.Objects;

/**
 * Subclass of {@link ItemViewModel}. Check out {@link ItemViewModel} for full hierarchy
 *
 * @param <T> Studentexam or Teacherexam?
 */
public abstract class ExamDocumentItemViewModel<T extends StudentExam> extends FilebackedItemViewModel<ExamDocumentItemViewModel.ExamDocumentItemLiveData> {
    ExamFactory factory;
    private T current;
    private String currentName;

    /**
     * Constructor
     *
     * @param application Host application of the Recyclerview
     */
    ExamDocumentItemViewModel(Application application) {
        super(application);
        this.livedata = new ExamDocumentItemLiveData(true);
    }

    /**
     * Open a given exam YAML
     *
     * @param name name of the YAML file
     */
    public void openExam(String name) {
        current = (T) factory.get(new File(examDir.getPath() + File.separator + name));
        currentName = name;
        this.livedata.refreshData(Objects.requireNonNull(current).toLiveDataSet(getApplication()), true);
    }

    /**
     * Write list back to YAML file
     */
    public void closeExam() {
        current.documentsFromLivedata(livedata.backingMap.values());
        factory.writeExamToFile(current, new File(examDir + File.separator + currentName));
    }

    /**
     * Get the current selected exam
     *
     * @return selected exam
     */
    public T getCurrent() {
        return current;
    }

    /**
     * {@link androidx.lifecycle.LiveData} backing a {@link androidx.recyclerview.widget.RecyclerView}
     * filled the {@link ThumbnailedExamDocument} tabs. These lists are the base of all file explorers in this app
     */
    public class ExamDocumentItemLiveData extends SelectableSortableItemLiveData<ExamDocument, ThumbnailedExamDocument> {
        ExamDocumentItemLiveData(boolean notificationNeeded) {
            super(notificationNeeded);
        }

        /**
         * Add a document to the {@link androidx.recyclerview.widget.RecyclerView}
         *
         * @param container the document
         * @param post      post the addition to the UI thread
         * @return
         */
        @Override
        public boolean add(ThumbnailedExamDocument container, boolean post) {
            while (!super.add(container, post)) {
                container.incrKey();
            }
            return true;
        }

        /**
         * Set a document rejected (in the students file browser, it is overlayed by a message)
         * and will be removed from the list, when the teacher locks his device
         *
         * @param index  index of document
         * @param reason rejection reason
         */
        void setRejected(int index, ThumbnailedExamDocument.RejectionReason reason) {
            Objects.requireNonNull(getValue()).get(index).reason = reason;
            toggleSelected(index);
        }
    }
}

