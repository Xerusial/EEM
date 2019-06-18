package edu.hm.eem_library.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.hm.eem_library.R;

import static java.lang.Math.sqrt;

public class ThumbnailedExamDocument extends SelectableSortableItem<ExamDocument> {
    @Nullable public final Bitmap thumbnail;
    public final boolean hasThumbnail;
    public RejectionReason reason;

    public enum RejectionReason{
        NONE,TOO_MANY_PAGES, HASH_DOES_NOT_MATCH, TOO_MANY_DOCS;
    }

    private ThumbnailedExamDocument(String sortableKey, ExamDocument item, @Nullable Bitmap thumbnail, boolean hasThumbnail) {
        super(sortableKey, item);
        this.thumbnail = thumbnail;
        this.hasThumbnail = hasThumbnail;
        reason = RejectionReason.NONE;
    }

    static ThumbnailedExamDocument getInstance(Context context, ExamDocument doc) {
        if(doc.getPath() == null){
            return new ThumbnailedExamDocument(doc.getName(), doc, null, false);
        } else {
                File file = new File(doc.getPath());
                if(file.isFile()){
                    return getThumb(context, file, doc);
                } else {
                    return new ThumbnailedExamDocument(doc.getName(), doc, null, true);
                }
        }
    }

    public static ThumbnailedExamDocument getInstance(Context context, int num_pages) {
        String name = context.getString(R.string.page_specified_document);
        ExamDocument doc = new ExamDocument(name, num_pages);
        return new ThumbnailedExamDocument(name, doc, null, false);
    }

    public static ThumbnailedExamDocument getInstance(Context context, Uri uri)
    {
            String path = URITOOLBOX.pathFromUri(context, uri);
            File file = new File(path);
            if(path!=null) {
                ExamDocument doc = new ExamDocument(new File(path).getName(), null, path);
                return getThumb(context, file, doc);
            }
            return null;
    }

    private static ThumbnailedExamDocument getThumb(Context context, File file, ExamDocument doc){
        try {
            PdfRenderer renderer = new PdfRenderer(context,file);
            int numberOfPages = renderer.getPageCount();
            Bitmap thumbnail = renderer.renderPage(0);
            renderer.close();
            FileInputStream fis = new FileInputStream(file);
            byte[] hash = HASHTOOLBOX.genMD5(fis);
            fis.close();
            doc.update(numberOfPages,hash);
            return new ThumbnailedExamDocument(doc.getName(), doc, thumbnail, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
