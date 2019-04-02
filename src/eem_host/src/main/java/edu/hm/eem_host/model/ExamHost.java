package edu.hm.eem_host.model;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class ExamHost extends SelectableItemHost {
    private Yaml yaml;
    private ExamDocumentListLiveData livedata;
    public ExamHost(Application application) {
        super(application);
        yaml = new Yaml(new Exam.ExamConstructor());
    }

    public class ExamDocumentListLiveData extends MutableLiveData<SelectableItemList<ExamDocument>> {
        private Exam current;
        ExamDocumentListLiveData(Exam current){
            this.current = current;
            setValue(new SelectableItemList<>(current.allowedDocuments));
        }

        public void add(ExamDocument doc){
            getValue().add(new SelectableItem<>(doc));
            //notify observers
            setValue(getValue());
        }

        public void removeSelected(){
            getValue().removeSelected();
            //notify observers
            setValue(getValue());
        }

        public Exam getCurrent() {
            return current;
        }

        void sync(){
            current.allowedDocuments = getValue().getRawList();
        }
    }

    public boolean setCurrent(String name) {
        boolean createNew = !readExamFromFile(name);
        if(createNew)
            this.livedata = new ExamDocumentListLiveData(new Exam(name, false,null));
        return createNew;
    }

    private boolean readExamFromFile(String name){
        boolean fileFound = true;
        File in = new File(examDir.getPath() + File.separator + name);
        try {
            FileInputStream fis = new FileInputStream(in);
            this.livedata = new ExamDocumentListLiveData((Exam)yaml.load(fis));
            fis.close();
        } catch (FileNotFoundException e){
            fileFound = false;
        } catch (IOException e){
            e.printStackTrace();
        }
        return fileFound;
    }

    public void writeExamToFile() {
        File out = new File(examDir.getPath() + File.separator + livedata.getCurrent().getName());
        try {
            FileWriter fw = new FileWriter(out);
            livedata.sync();
            yaml.dump(livedata.getCurrent(), fw);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ExamDocumentListLiveData getLivedata() {
        return livedata;
    }
}
