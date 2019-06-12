package edu.hm.eem_library.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.hm.eem_library.R;

import static java.lang.Math.sqrt;

public class ThumbnailedExamDocument extends SortableItem<ExamDocument> {
    @Nullable public final Bitmap thumbnail;
    public final boolean hasThumbnail;

    private ThumbnailedExamDocument(String sortableKey, ExamDocument item, @Nullable Bitmap thumbnail, boolean hasThumbnail) {
        super(sortableKey, item);
        this.thumbnail = thumbnail;
        this.hasThumbnail = hasThumbnail;
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

    public static ThumbnailedExamDocument getInstance(Context context, String name, Uri uri)
    {
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            String[] segment = uri.getLastPathSegment().split(":");
            if(segment.length == 2)
            {
                if("primary".equalsIgnoreCase(segment[0]))
                {
                    String path = Environment.getExternalStorageDirectory() + "/" + segment[1];
                    ExamDocument doc = new ExamDocument(name, null, path);
                    return getThumb(context, fileDescriptor, doc);
                }
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
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);
            numberOfPages = renderer.getPageCount();
            PdfRenderer.Page page = renderer.openPage(0);
            page.render(thumbnail, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            byte[] hash = HASHTOOLBOX.genMD5(new ParcelFileDescriptor.AutoCloseInputStream(fileDescriptor));
            renderer.close();
            doc.update(numberOfPages,hash);
            return new ThumbnailedExamDocument(doc.getName(), doc, thumbnail, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
