package edu.hm.eem_library.model;

import androidx.annotation.Nullable;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class ExamFactory {
    public enum ExamType {
        STUDENT, TEACHER
    }

    private final Yaml yaml;
    private final ExamType type;

    public ExamFactory(ExamType type) {
        this.type = type;
        switch (type) {
            case TEACHER:
                yaml = new Yaml(new TeacherExam.ExamConstructor());
                break;
            default:
                yaml = new Yaml(new StudentExam.ExamConstructor());
        }
    }

    @Nullable
    StudentExam get(File examDir, String name) {
        File in = new File(examDir.getPath() + File.separator + name);
        StudentExam retval = null;
        try {
            FileInputStream fis = new FileInputStream(in);
            retval = yaml.load(fis);
            fis.close();
        } catch (FileNotFoundException e) {
            switch (type) {
                case TEACHER:
                    retval = new TeacherExam();
                    break;
                default:
                    retval = new StudentExam();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retval;
    }

    <T extends StudentExam> void writeExamToFile(T exam, File examDir, String name) {
        File out = new File(examDir.getPath() + File.separator + name);
        try {
            FileWriter fw = new FileWriter(out);
            yaml.dump(exam, fw);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    <T extends StudentExam> void createSendableVersion(File mainDir, File examDir, String name){
        T exam = (T) get(examDir, name);
        for(ExamDocument doc : exam.getAllowedDocuments()){
            doc.removePath();
        }
        writeExamToFile(exam, mainDir, "sendable_exam");
    }
}
