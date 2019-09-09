package edu.hm.eem_library.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;
import java.util.TreeSet;

import edu.hm.eem_library.R;
import edu.hm.eem_library.view.DocumentPickerActivity;

import static java.lang.Math.sqrt;

public class ThumbnailedExamDocument extends SelectableSortableItem<ExamDocument> {
    @Nullable
    public final Bitmap thumbnail;
    public final boolean hasThumbnail;
    public RejectionReason reason;

    private ThumbnailedExamDocument(String sortableKey, ExamDocument item, @Nullable Bitmap thumbnail, boolean hasThumbnail) {
        super(sortableKey, item);
        this.thumbnail = thumbnail;
        this.hasThumbnail = hasThumbnail;
        reason = RejectionReason.NONE;
    }

    static void loadInstances(Context context, TreeSet<ThumbnailedExamDocument> outSet, LinkedList<ExamDocument> inList) {
        for (ExamDocument doc : inList) {
            if (doc.getUriString() == null) {
                outSet.add(new ThumbnailedExamDocument(doc.getName(), doc, null, false));
            } else {
                try {
                    Uri uri = Uri.parse(doc.getUriString());
                    ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
                    boolean documentChanged = getMetaFromUri(context, uri).lastModifiedDate.after(doc.getHashCreationDate());
                    outSet.add(getThumb(context, fileDescriptor, doc, HASHTOOLBOX.WhichHash.fromDoc(doc), documentChanged));
                } catch (FileNotFoundException e) {
                    outSet.add(new ThumbnailedExamDocument(doc.getName(), doc, null, true));
                }
            }
        }
    }

    public static ThumbnailedExamDocument getInstance(Context context, int num_pages) {
        String name = context.getString(R.string.page_specified_document);
        ExamDocument doc = new ExamDocument(name, num_pages);
        return new ThumbnailedExamDocument(name, doc, null, false);
    }

    @Nullable
    public static ThumbnailedExamDocument getInstance(Context context, Uri uri, HASHTOOLBOX.WhichHash which) {
        ThumbnailedExamDocument thDoc;
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            ExamDocument doc = new ExamDocument(getMetaFromUri(context, uri).name, uri.toString());
            thDoc = getThumb(context, fileDescriptor, doc, which, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            thDoc = null;
        }
        return thDoc;
    }

    private static ThumbnailedExamDocument getThumb(Context context, ParcelFileDescriptor fileDescriptor, ExamDocument doc, HASHTOOLBOX.WhichHash which, boolean documentChanged) {
        try {
            int width = (int) (context.getResources().getDisplayMetrics().density * 180); // 180dp
            Bitmap thumbnail = Bitmap.createBitmap(width, (int) sqrt(2) * width, Bitmap.Config.ARGB_8888);
            PdfRenderer renderer = new PdfRenderer(context, fileDescriptor);
            PdfRenderer.Page page = renderer.openPage(0);
            page.render(thumbnail);
            page.close();
            if (documentChanged) {
                ExamDocument.Identifiers ids = HASHTOOLBOX.genDocMD5s(context, new ParcelFileDescriptor.AutoCloseInputStream(fileDescriptor), which);
                Objects.requireNonNull(ids).pages = renderer.getPageCount();
                ids.hashCreationDate = Calendar.getInstance().getTime();
                doc.update(ids);
            }
            renderer.close();
            return new ThumbnailedExamDocument(doc.getName(), doc, thumbnail, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DocumentPickerActivity.Meta getMetaFromUri(Context context, Uri uri) {
        DocumentPickerActivity.Meta ret = new DocumentPickerActivity.Meta();
        String path = null;
        if (Objects.equals(uri.getAuthority(), "com.android.providers.downloads.documents")) {
            // Cursor.close not needed because of Java 7 automatic resource management
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                }
            }
        } else {
            String[] split = Objects.requireNonNull(uri.getPath()).split(":");
            path = split[split.length - 1];
        }
        String[] split = Objects.requireNonNull(path).split(File.separator);
        ret.name = split[split.length - 1];
        ret.lastModifiedDate = new Date(new File(path).lastModified());
        return ret;
    }

    public enum RejectionReason {
        NONE, TOO_MANY_PAGES, HASH_DOES_NOT_MATCH, TOO_MANY_DOCS
    }

}
