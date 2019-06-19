package edu.hm.eem_library.model;

import androidx.annotation.Nullable;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import edu.hm.eem_library.net.FilePacket;

public class ExamFactory {
    public enum ExamType {
        STUDENT, TEACHER
    }

    private final Yaml yaml;
    private final ExamType type;

    public ExamFactory(ExamType type) {
        this.type = type;
        if (type == ExamType.TEACHER) {
            yaml = new Yaml(new TeacherExam.ExamConstructor());
        } else {
            yaml = new Yaml(new StudentExam.ExamConstructor());
        }
    }

    @Nullable
    StudentExam get(File examDir, String name) {
        File in = new File(examDir.getPath() + File.separator + name);
        StudentExam retval = null;
        try {
            FileInputStream fis = new FileInputStream(in);
            retval = extract(fis);
            fis.close();
        } catch (FileNotFoundException e) {
            if (type == ExamType.TEACHER) {
                retval = new TeacherExam();
            } else {
                retval = new StudentExam();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retval;
    }

    public StudentExam extract(InputStream is){
        return yaml.load(is);
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

    public <T extends StudentExam> void createSendableVersion(File mainDir, File examDir, String name){
        T exam = (T) get(examDir, name);
        for(ExamDocument doc : exam.getAllowedDocuments()){
            doc.removeUriString();
        }
        writeExamToFile(exam, mainDir, FilePacket.FILENAME);
    }
}
