package edu.hm.eem_library.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeSet;

import edu.hm.eem_library.R;
import edu.hm.eem_library.view.DocumentPickerActivity;

import static java.lang.Math.sqrt;

public class ThumbnailedExamDocument extends SelectableSortableItem<ExamDocument> {
    @Nullable public final Bitmap thumbnail;
    public final boolean hasThumbnail;
    public RejectionReason reason;

    public enum RejectionReason{
        NONE,TOO_MANY_PAGES, HASH_DOES_NOT_MATCH, TOO_MANY_DOCS
    }

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
                    outSet.add(getThumb(context, fileDescriptor, doc));
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
    public static ThumbnailedExamDocument getInstance(DocumentPickerActivity context, Uri uri)
    {
        ThumbnailedExamDocument thDoc;
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            ExamDocument doc = new ExamDocument(context.getNameFromUri(uri), uri.toString());
            thDoc = getThumb(context, fileDescriptor, doc);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            thDoc = null;
        }
        return thDoc;
    }

    private static ThumbnailedExamDocument getThumb(Context context, ParcelFileDescriptor fileDescriptor, ExamDocument doc){
        try {
            int width = context.getResources().getDisplayMetrics().widthPixels/2;
            Bitmap thumbnail = Bitmap.createBitmap(width,(int)sqrt(2)*width, Bitmap.Config.ARGB_8888);
            PdfRenderer renderer = new PdfRenderer(context,fileDescriptor);
            PdfRenderer.Page page = renderer.openPage(0);
            page.render(thumbnail);
            page.close();
            ExamDocument.Identifiers ids = HASHTOOLBOX.genDocMD5s(context, new ParcelFileDescriptor.AutoCloseInputStream(fileDescriptor));
            renderer.close();
            doc.update(ids);
            return new ThumbnailedExamDocument(doc.getName(), doc, thumbnail, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



}
