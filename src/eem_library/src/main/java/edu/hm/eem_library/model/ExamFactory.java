package edu.hm.eem_library.model;

import androidx.annotation.Nullable;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Generifies the creation of exam objects from YAML
 */
public class ExamFactory {
    private final Yaml yaml;
    private final ExamType type;

    /**
     * Creates specific YAML constructors for the teacher and student exams
     *
     * @param type stduent or teacher?
     */
    public ExamFactory(ExamType type) {
        this.type = type;
        if (type == ExamType.TEACHER) {
            yaml = new Yaml(new TeacherExam.ExamConstructor());
        } else {
            yaml = new Yaml(new StudentExam.ExamConstructor());
        }
    }

    /**
     * Parse exam from input YAML file
     *
     * @param in input file
     * @return an exam created from input file
     */
    @Nullable
    StudentExam get(File in) {
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

    /**
     * Create an exam from an {@link InputStream}
     *
     * @param is inputstream
     * @return created exam
     */
    public StudentExam extract(InputStream is) {
        return yaml.load(is);
    }

    /**
     * Writes an exam to a file
     *
     * @param exam exam to be written
     * @param out  output file handle
     * @param <T>  Student or Teacher?
     */
    public <T extends StudentExam> void writeExamToFile(T exam, File out) {
        try {
            FileWriter fw = new FileWriter(out);
            yaml.dump(exam, fw);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Strip down a teacher exam and remove uri as well as timestamp
     *
     * @param in  input YAML
     * @param out output YAML
     * @param <T> student or teacher?
     */
    public <T extends StudentExam> void createSendableVersion(File in, File out) {
        T exam = (T) get(in);
        for (ExamDocument doc : Objects.requireNonNull(exam).getAllowedDocuments()) {
            doc.removeUriString();
            doc.removeHashCreationDate();
        }
        writeExamToFile(exam, out);
    }

    /**
     * Types of exams: Teacherexams contain a password hash and salt
     */
    public enum ExamType {
        STUDENT, TEACHER
    }
}
