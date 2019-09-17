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

/**
 * A Subclass of Examdocument with selectable property. Mainly used to add a thumbnail bitmap
 * for display purposes
 */
public class ThumbnailedExamDocument extends SelectableSortableItem<ExamDocument> {
    @Nullable
    public final Bitmap thumbnail;
    public final boolean hasThumbnail;
    public RejectionReason reason;

    /**
     * Constructor
     *
     * @param sortableKey unique Key
     * @param item containing examdocument
     * @param thumbnail thumbnail bitmap, if one exists
     * @param hasThumbnail boolean specifying if thumbnail will be added later
     */
    private ThumbnailedExamDocument(String sortableKey, ExamDocument item, @Nullable Bitmap thumbnail, boolean hasThumbnail) {
        super(sortableKey, item);
        this.thumbnail = thumbnail;
        this.hasThumbnail = hasThumbnail;
        reason = RejectionReason.NONE;
    }

    /**
     * Load all thumbnails an refresh hashes of given document list
     *
     * @param context calling activity
     * @param outSet output set of thumbnailed examdocuments
     * @param inList input list of documents
     */
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

    /**
     * Load single page specified document (has no thumbnail
     *
     * @param context calling activity
     * @param num_pages number of pages
     * @return a thumbnailed document
     */
    public static ThumbnailedExamDocument getInstance(Context context, int num_pages) {
        String name = context.getString(R.string.page_specified_document);
        ExamDocument doc = new ExamDocument(name, num_pages);
        return new ThumbnailedExamDocument(name, doc, null, false);
    }

    /**
     * Load single uri specified document
     *
     * @param context calling activity
     * @param uri specifying uri
     * @param which which hash to create from uri
     * @return a thumbnailed document
     */
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

    /**
     *  Get a thumbnail and refresh meta of document
     *
     * @param context calling activity
     * @param fileDescriptor file descriptor of PDF
     * @param doc origin of Uri
     * @param which which hash to generate
     * @param documentChanged does meta need to be updated?
     * @return a thumbnailed document
     */
    private static ThumbnailedExamDocument getThumb(Context context, ParcelFileDescriptor fileDescriptor, ExamDocument doc, HASHTOOLBOX.WhichHash which, boolean documentChanged) {
        try {
            int width = (int) (context.getResources().getDisplayMetrics().density * 180); // 180dp
            Bitmap thumbnail = Bitmap.createBitmap(width, (int) sqrt(2) * width, Bitmap.Config.ARGB_8888);
            PdfRenderer renderer = new PdfRenderer(context, fileDescriptor);
            PdfRenderer.Page page = renderer.openPage(0);
            page.render(thumbnail, 0, 0, 1);
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

    /**
     * Receive name and timestamp from a URI. This depends on the content provider
     *
     * @param context calling activity
     * @param uri source
     * @return Meta object with name and timestamp
     */
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

    /**
     * Reason a document was not accepted by meta matching
     */
    public enum RejectionReason {
        NONE, TOO_MANY_PAGES, HASH_DOES_NOT_MATCH, TOO_MANY_DOCS
    }

}
