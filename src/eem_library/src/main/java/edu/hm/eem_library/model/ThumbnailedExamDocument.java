package edu.hm.eem_library.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Pair;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

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
            try {
                File file = new File(doc.getPath());
                ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);
                return getThumb(context, fileDescriptor, doc);
            } catch (FileNotFoundException e){
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
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            String path = URITOOLBOX.pathFromUri(context, uri);
            if(path!=null) {
                ExamDocument doc = new ExamDocument(new File(path).getName(), null, null, path);
                return getThumb(context, fileDescriptor, doc);
            }
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ThumbnailedExamDocument getThumb(Context context, ParcelFileDescriptor fileDescriptor, ExamDocument doc){
        try {
            int numberOfPages, width = context.getResources().getDisplayMetrics().widthPixels/2;
            Bitmap thumbnail = Bitmap.createBitmap(width,(int)sqrt(2)*width, Bitmap.Config.ARGB_8888);
            PdfRenderer renderer = new PdfRenderer(context,fileDescriptor);
            numberOfPages = renderer.getPageCount();
            PdfRenderer.Page page = renderer.openPage(0);
            page.render(thumbnail);
            page.close();
            Pair<byte[], byte[]> hashes = HASHTOOLBOX.genMD5s(context, new ParcelFileDescriptor.AutoCloseInputStream(fileDescriptor));
            renderer.close();
            doc.update(numberOfPages,hashes);
            return new ThumbnailedExamDocument(doc.getName(), doc, thumbnail, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



}
