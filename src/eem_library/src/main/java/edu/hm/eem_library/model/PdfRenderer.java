package edu.hm.eem_library.model;

import android.content.Context;
import android.graphics.Bitmap;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

import java.io.File;
import java.io.IOException;

/** Wrapper class for {@link PDDocument}
 */
public class PdfRenderer {
    private final PDFRenderer renderer;
    private final PDDocument pdDocument;

    public PdfRenderer(Context context, File file) throws IOException {
        this.pdDocument = PDDocument.load(file);
        renderer = new PDFRenderer(pdDocument);
    }

    public void close() throws IOException{
        pdDocument.close();
    }

    public int getPageCount(){
        return pdDocument.getNumberOfPages();
    }

    public Bitmap renderPage(int pageNum) throws IOException{
        return renderer.renderImage(pageNum, 1, Bitmap.Config.RGB_565);
    }
}
