package edu.hm.eem_library.model;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

public class ThumbnailedExamDocument extends SortableItem<String, ExamDocument> {
    public final Bitmap thumbnail;

    public ThumbnailedExamDocument(String sortableKey, ExamDocument item, @Nullable Bitmap thumbnail) {
        super(sortableKey, item);
        this.thumbnail = thumbnail;
    }
}
