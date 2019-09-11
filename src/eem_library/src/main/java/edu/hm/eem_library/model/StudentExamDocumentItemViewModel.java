package edu.hm.eem_library.model;

import android.app.Application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Subclass of {@link ItemViewModel}. Check out {@link ItemViewModel} for full hierarchy
 */
public class StudentExamDocumentItemViewModel extends ExamDocumentItemViewModel<StudentExam> {
    private static final int CODE_ACCEPTED = 0;
    private static final int CODE_REJECTED_TOO_MANY_PAGES = -1;
    public TeacherExam teacherExam;

    /**
     * Constructor
     *
     * @param application host activity
     */
    public StudentExamDocumentItemViewModel(Application application) {
        super(application);
        factory = new ExamFactory(ExamFactory.ExamType.STUDENT);
    }

    /**
     * Checks the current selected Examlist against a given list from a teacher.
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
            for (int i = 0; i < Objects.requireNonNull(studentlist).size(); i++) {
                ExamDocument doc = studentlist.get(i).item;
                metas.add(new Meta(i, doc.getPages(), doc.getHash(), doc.getNonAnnotatedHash()));
            }
            for (ExamDocument teacherdoc : exam.getAllowedDocuments()) {
                byte[] hash = teacherdoc.getHash();
                byte[] nonAnnotatedHash = teacherdoc.getNonAnnotatedHash();
                if (hash != null && nonAnnotatedHash == null) {
                    for (Meta meta : metas) {
                        // if hash matches, set document to allowed
                        if (Arrays.equals(hash, meta.hash)) {
                            meta.pages = CODE_ACCEPTED;
                            break;
                        }
                    }
                } else if (nonAnnotatedHash != null) {
                    for (Meta meta : metas) {
                        // if hash matches, set document to allowed
                        if (Arrays.equals(nonAnnotatedHash, meta.nonAnnotatedHash)) {
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
            if (!cachedPages.isEmpty()) {
                for (Meta m : metas) {
                    if (m.pages > CODE_ACCEPTED) {
                        if (m.pages <= cachedPages.getFirst()) {
                            m.pages = CODE_ACCEPTED;
                            cachedPages.removeFirst();
                        } else {
                            m.pages = CODE_REJECTED_TOO_MANY_PAGES;
                        }
                        //All after this will be hash specified
                    } else break;
                }
            }
            getLivedata().clearSelection();
            boolean tooManyDocs = studentlist.size() > teacherlist.size();
            for (Meta m : metas) {
                if (m.pages != CODE_ACCEPTED) {
                    //Select all documents, that were accepted
                    if (m.pages == CODE_REJECTED_TOO_MANY_PAGES)
                        getLivedata().setRejected(m.index, ThumbnailedExamDocument.RejectionReason.TOO_MANY_PAGES);
                    else if (tooManyDocs) {
                        getLivedata().setRejected(m.index, ThumbnailedExamDocument.RejectionReason.TOO_MANY_DOCS);
                    } else {
                        getLivedata().setRejected(m.index, ThumbnailedExamDocument.RejectionReason.HASH_DOES_NOT_MATCH);
                    }
                    ret = false;
                }
            }
        }
        return ret;
    }

    /**
     * Helper struct with comparator for the checkExam
     */
    private class Meta implements Comparable<Meta> {
        private int index, pages;
        private byte[] hash, nonAnnotatedHash;

        private Meta(int index, int pages, byte[] hash, byte[] nonAnnotatedHash) {
            this.index = index;
            this.pages = pages;
            this.hash = hash;
            this.nonAnnotatedHash = nonAnnotatedHash;
        }

        @Override
        public int compareTo(Meta o) {
            return Integer.compare(pages, o.pages);
        }
    }
}
