package edu.hm.eem_client.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.PdfRenderer;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReaderFragment extends Fragment {

    private String path;
    private FloatingActionButton pageForward, pageBackward;
    private ImageView readerPage;
    private PdfRenderer renderer = null;
    private int pageCount, currentPage;
    private Bitmap pageBitmap;
    private Handler handler;

    public ReaderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reader, container, false);
        pageForward = view.findViewById(R.id.page_forward);
        pageBackward = view.findViewById(R.id.page_backwards);
        readerPage = view.findViewById(R.id.reader_page);
        if(pageCount>1) {
            pageForward.setOnClickListener(v -> turnPage(true));
            pageBackward.setOnClickListener(v -> turnPage(false));
        } else {
            pageForward.hide();
            pageBackward.hide();
        }
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        pageBitmap = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.ARGB_8888);
        readerPage.setImageBitmap(pageBitmap);
        renderPage();
        enableButton(pageBackward, false);
        handler = new Handler(Looper.getMainLooper());
        return view;
    }

    private void turnPage(boolean forward) {
        boolean endNotReached = currentPage != pageCount;
        boolean beginningNotReached = currentPage != 0;
        if (forward && endNotReached) {
            currentPage++;
            renderPage();
            enableButton(pageForward, currentPage != pageCount);
            enableButton(pageBackward, true);
        } else if (!forward && beginningNotReached) {
            currentPage--;
            renderPage();
            enableButton(pageBackward, currentPage != 0);
            enableButton(pageForward, true);
        }

    }

    void turnPageAsync(boolean forward){
        handler.post(()-> turnPage(forward));
    }

    private void enableButton(FloatingActionButton b, boolean enable) {
        b.setEnabled(enable);
        b.setAlpha(enable ? 1.0f : 0.5f);
    }

    private boolean renderPage() {
        boolean ret = false;
        if (renderer != null) {
            PdfRenderer.Page page = renderer.openPage(currentPage);
            page.render(pageBitmap);
            page.close();
            ret = true;
        }
        return ret;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        path = getArguments().getString(DocumentExplorerFragment.EXAMDOCUMENT_FIELD);
        File file = new File(path);
        ParcelFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(context,fileDescriptor);
            currentPage = 0;
            pageCount = renderer.getPageCount()-1;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (renderer != null) renderer.close();
    }
}
