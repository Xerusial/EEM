package edu.hm.eem_client.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import java.util.Objects;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.PdfRenderer;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReaderFragment extends Fragment {

    private FloatingActionButton pageForward, pageBackward;
    private PdfRenderer renderer = null;
    private int pageCount, currentPage;
    private Bitmap pageBitmap;

    public ReaderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reader, container, false);
        pageForward = view.findViewById(R.id.page_forward);
        pageBackward = view.findViewById(R.id.page_backwards);
        ImageView readerPage = view.findViewById(R.id.reader_page);
        if(pageCount>1) {
            pageForward.setOnClickListener(v -> turnPage(true));
            pageBackward.setOnClickListener(v -> turnPage(false));
        } else {
            pageForward.hide();
            pageBackward.hide();
        }
        DisplayMetrics metrics = Objects.requireNonNull(getContext()).getResources().getDisplayMetrics();
        pageBitmap = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.ARGB_8888);
        readerPage.setImageBitmap(pageBitmap);
        renderPage();
        enableButton(pageBackward, false);
        return view;
    }

    void turnPage(boolean forward) {
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

    private void enableButton(FloatingActionButton b, boolean enable) {
        b.setEnabled(enable);
        b.setAlpha(enable ? 1.0f : 0.5f);
    }

    private void renderPage() {
        if (renderer != null) {
            PdfRenderer.Page page = renderer.openPage(currentPage);
            page.render(pageBitmap);
            page.close();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        String path = Objects.requireNonNull(getArguments()).getString(DocumentExplorerFragment.EXAMDOCUMENT_FIELD);
        File file = new File(path);
        ParcelFileDescriptor fileDescriptor;
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
