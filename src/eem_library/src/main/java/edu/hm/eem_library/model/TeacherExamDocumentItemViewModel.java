package edu.hm.eem_library.model;

import android.app.Application;

public class TeacherExamDocumentItemViewModel extends ExamDocumentItemViewModel<TeacherExam> {

    public TeacherExamDocumentItemViewModel(Application application) {
        super(application);
        factory = new ExamFactory(ExamFactory.ExamType.TEACHER);
    }
}
