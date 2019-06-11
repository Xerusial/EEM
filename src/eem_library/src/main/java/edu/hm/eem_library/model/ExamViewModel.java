package edu.hm.eem_library.model;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ExamViewModel extends FilebackedItemViewModel<ExamViewModel.ExamDocumentLiveData> {
    private TeacherExam current;
    private String currentName;
    private static final Yaml yaml = new Yaml(new TeacherExam.ExamConstructor());

    public ExamViewModel(Application application) {
        super(application);
    }

    public class ExamDocumentLiveData extends SelectableSortableMapLiveData<String, ExamDocument, ThumbnailedExamDocument>{
        ExamDocumentLiveData(@Nullable Set<ThumbnailedExamDocument> set, boolean notificationNeeded) {
            super(set, notificationNeeded);
        }

        @Override
        public boolean add(String sortableKey, ThumbnailedExamDocument container, boolean post) {
            while(!super.add(sortableKey, container, post)){
                container.item.incrName();
                sortableKey = container.item.getName();
            }
            return true;
        }
    }

    public void openExam(String name) {
        current = new TeacherExam(yaml,examDir,name);
        currentName = name;
        this.livedata = new ExamDocumentLiveData(current.toLiveDataSet(getApplication()), true);
    }

    public void closeExam(){
        current.documentsFromLivedata(livedata.backingMap.values());
        current.writeExamToFile(yaml, examDir, currentName);
    }

    public TeacherExam getCurrent() {
        return current;
    }
}
