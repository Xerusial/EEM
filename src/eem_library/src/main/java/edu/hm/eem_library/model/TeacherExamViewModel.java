package edu.hm.eem_library.model;

import android.app.Application;

public class TeacherExamViewModel extends ExamViewModel<TeacherExam>{

    public TeacherExamViewModel(Application application) {
        super(application);
        factory = new ExamFactory(ExamFactory.ExamType.TEACHER);
    }
}
