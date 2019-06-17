package edu.hm.eem_library.model;

import android.app.Application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class StudentExamViewModel extends ExamViewModel<StudentExam> {
    public TeacherExam teacherExam;

    public StudentExamViewModel(Application application) {
        super(application);
        factory = new ExamFactory(ExamFactory.ExamType.STUDENT);
    }

    private class Meta implements Comparable<Meta> {
        private int index, pages;
        private byte[] hash;

        public Meta(int index, int pages, byte[] hash) {
            this.index = index;
            this.pages = pages;
            this.hash = hash;
        }

        @Override
        public int compareTo(Meta o) {
            return Integer.compare(pages, o.pages);
        }
    }

    private static final int CODE_ACCEPTED = 0;
    private static final int CODE_REJECTED_TOO_MANY_DOCS = -1;
    private static final int CODE_REJECTED_TOO_MANY_PAGES = -2;
    /**
     * Checks the current selected Examlist against a give list from a teacher.
     *
     * @param exam {@link TeacherExam} to check against
     * @return true if all documents were accepted
     */
    public boolean checkExam(TeacherExam exam) {
        boolean ret = true;
        teacherExam = exam;
        LinkedList<ExamDocument> teacherlist = exam.getAllowedDocuments();
        // Empty list means all allowed!
        if (teacherlist.size() != 0) {
            LinkedList<Integer> cachedPages = new LinkedList<>();
            LinkedList<Meta> metas = new LinkedList<>();
            ArrayList<ThumbnailedExamDocument> studentlist = (ArrayList<ThumbnailedExamDocument>) this.getLivedata().getValue();
            for (int i = 0; i < studentlist.size(); i++) {
                metas.add(new Meta(i, studentlist.get(i).item.getPages(), studentlist.get(i).item.getHash()));
            }
            for (ExamDocument teacherdoc : exam.getAllowedDocuments()) {
                byte[] hash = teacherdoc.getHash();
                if (hash != null) {
                    for (Meta meta : metas) {
                        // if hash matches, set document to allowed
                        if (Arrays.equals(hash, meta.hash)) {
                            meta.pages = CODE_ACCEPTED;
                            break;
                        }
                    }
                } else {
                    // No hash: must be Page specified document
                    cachedPages.add(teacherdoc.getPages());
                }

            }
            //Sort both in descending page number: hash qualified docs will be last in list
            Collections.sort(cachedPages, Collections.reverseOrder());
            Collections.sort(metas, Collections.reverseOrder());
            for (Meta m : metas) {
                if (m.pages > CODE_ACCEPTED) {
                    if(cachedPages.isEmpty()){
                        m.pages = CODE_REJECTED_TOO_MANY_DOCS;
                    } else if (m.pages <= cachedPages.getFirst()) {
                        m.pages = CODE_ACCEPTED;
                        cachedPages.removeFirst();
                    } else {
                        m.pages = CODE_REJECTED_TOO_MANY_PAGES;
                    }
                    //All after this will be hash specified
                } else break;
            }
            getLivedata().clearSelection();
            for (Meta m : metas) {
                if (m.pages != CODE_ACCEPTED) {
                    //Select all documents, that were accepted
                    if(m.pages == CODE_REJECTED_TOO_MANY_DOCS){
                        getLivedata().setRejected(m.index, ThumbnailedExamDocument.RejectionReason.TOO_MANY_DOCS);
                    } else if(m.pages == CODE_REJECTED_TOO_MANY_PAGES)
                        getLivedata().setRejected(m.index, ThumbnailedExamDocument.RejectionReason.TOO_MANY_PAGES);
                    else
                        getLivedata().setRejected(m.index, ThumbnailedExamDocument.RejectionReason.HASH_DOES_NOT_MATCH);
                    ret = false;
                }
            }
        }
        return ret;
    }
}
