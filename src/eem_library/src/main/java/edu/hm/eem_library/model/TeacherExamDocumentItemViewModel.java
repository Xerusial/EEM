package edu.hm.eem_library.model;

import android.app.Application;

/**
 * Subclass of {@link ItemViewModel}. Check out {@link ItemViewModel} for full hierarchy
 * This Viewmodel holds teacherexam references
 */
public class TeacherExamDocumentItemViewModel extends ExamDocumentItemViewModel<TeacherExam> {

    public TeacherExamDocumentItemViewModel(Application application) {
        super(application);
        factory = new ExamFactory(ExamFactory.ExamType.TEACHER);
    }
}
