package edu.hm.eem_library.model;

import android.app.Application;

import androidx.annotation.Nullable;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class ExamViewModel extends FilebackedItemViewModel<ExamViewModel.ExamDocumentLiveData> {
    private Yaml yaml;
    private Exam current;
    private String currentName;
    public ExamViewModel(Application application) {
        super(application);
        yaml = new Yaml(new Exam.ExamConstructor());
    }

    public class ExamDocumentLiveData extends SelectableSortableMapLiveData<String, ExamDocument>{
        ExamDocumentLiveData(@Nullable Set<SortableItem<String, ExamDocument>> set, boolean notificationNeeded) {
            super(set, notificationNeeded);
        }

        @Override
        public boolean add(String sortableKey, ExamDocument item, boolean post) {
            while(!super.add(sortableKey, item, post)){
                item.incrName();
                sortableKey = item.getName();
            }
            return true;
        }
    }

    public boolean openExam(String name) {
        currentName = name;
        boolean createNew = !readExamFromFile(name);
        if(createNew) current = new Exam(false, null);
        this.livedata = new ExamDocumentLiveData(current.toLiveDataSet(), true);
        return createNew;
    }

    private boolean readExamFromFile(String name){
        boolean fileFound = true;
        File in = new File(examDir.getPath() + File.separator + name);
        try {
            FileInputStream fis = new FileInputStream(in);
            current = yaml.load(fis);
            fis.close();
        } catch (FileNotFoundException e){
            fileFound = false;
        } catch (IOException e){
            e.printStackTrace();
        }
        return fileFound;
    }

    public void writeExamToFile() {
        File out = new File(examDir.getPath() + File.separator + currentName);
        try {
            FileWriter fw = new FileWriter(out);
            current.documentsFromLivedata(livedata.backingMap.values());
            yaml.dump(current, fw);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Exam getCurrent() {
        return current;
    }
}
