package edu.hm.eem_library.model;

import android.app.Application;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class ExamViewModel extends FilebackedItemViewModel<SelectableSortableMapLiveData<String, ExamDocument>> {
    private Yaml yaml;
    private Exam current;
    private String currentName;
    public ExamViewModel(Application application) {
        super(application);
        yaml = new Yaml(new Exam.ExamConstructor());
    }

    public boolean openExam(String name) {
        currentName = name;
        boolean createNew = !readExamFromFile(name);
        if(createNew) current = new Exam(false, null);
        this.livedata = new SelectableSortableMapLiveData<>(ExamDocument.toSet(current.allowedDocuments));
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
