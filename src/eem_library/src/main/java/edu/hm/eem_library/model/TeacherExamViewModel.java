package edu.hm.eem_library.model;

import android.app.Application;

import org.yaml.snakeyaml.Yaml;

public class TeacherExamViewModel extends ExamViewModel<TeacherExam>{

    public TeacherExamViewModel(Application application) {
        super(application);
        factory = new ExamFactory(ExamFactory.ExamType.TEACHER);
    }
}
