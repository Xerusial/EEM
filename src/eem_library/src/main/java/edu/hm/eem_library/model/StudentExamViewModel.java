package edu.hm.eem_library.model;

import android.app.Application;

import org.yaml.snakeyaml.Yaml;

public class StudentExamViewModel extends ExamViewModel<StudentExam>{

    public StudentExamViewModel(Application application) {
        super(application);
        factory = new ExamFactory(ExamFactory.ExamType.STUDENT);
    }
}
