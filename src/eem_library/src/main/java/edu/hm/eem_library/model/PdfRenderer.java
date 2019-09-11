package edu.hm.eem_library.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.util.Size;

import java.io.IOException;


/**
 * Wrapper class for {@link PdfiumCore} to make it behave similar to {@link android.graphics.pdf.PdfRenderer}.
 * {@link android.graphics.pdf.PdfRenderer} does not work correctly on Devices with API lower
 * than Oreo (8.0).
 */
public class PdfRenderer {
    private final PdfiumCore pdfiumCore;
    private final PdfDocument pdfDocument;

    /**
     * Constructor
     *
     * @param context calling Activity
     * @param fd filedescriptor containing PDF
     * @throws IOException if filedescriptor can't be opened
     */
    public PdfRenderer(Context context, ParcelFileDescriptor fd) throws IOException {
        this.pdfiumCore = new PdfiumCore(context);
        this.pdfDocument = pdfiumCore.newDocument(fd);
    }

    /**
     * Close the document
     */
    public void close() {
        pdfiumCore.closeDocument(pdfDocument);
    }

    /**
     * Get number of pages
     *
     * @return number of pages
     */
    public int getPageCount() {
        return pdfiumCore.getPageCount(pdfDocument);
    }

    /**
     * Get a page object
     *
     * @param pageNum number of page to open
     * @return page object
     */
    public Page openPage(int pageNum) {
        return new Page(pageNum);
    }

    /**
     * Page class similar to {@link android.graphics.pdf.PdfRenderer}'s
     */
    public class Page {
        private final int pageNum;

        /**
         * Constructor
         *
         * @param pageNum page number
         */
        private Page(int pageNum) {
            pdfiumCore.openPage(pdfDocument, pageNum);
            this.pageNum = pageNum;
        }

        /**
         * Render this page
         *
         * @param bitmap bitmap to render on
         */
        public void render(Bitmap bitmap) {
            pdfiumCore.openPage(pdfDocument, pageNum);
            Size s = pdfiumCore.getPageSize(pdfDocument, pageNum);
            float scaler = ((float) bitmap.getWidth()) / s.getWidth();
            int height = (int) (s.getHeight() * scaler);
            int startY = bitmap.getHeight() / 2 - height / 2;
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, startY, bitmap.getWidth(), height, true);
        }

        /**
         * Method only here for compatibility reasons
         */
        public void close() {

        }
    }
}

