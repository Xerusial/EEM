package edu.hm.eem_client.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.PdfRenderer;

/** Fragment used to display the PDFs in the exam.
 *
 */
public class ReaderFragment extends Fragment {

    private FloatingActionButton pageForward, pageBackward;
    private PdfRenderer renderer = null;
    private int pageCount, currentPage;
    private Bitmap pageBitmap, previewBitmap;
    private ImageView readerPage;
    private ConstraintLayout seekBarDrawer;
    private SeekBar seekBar;
    private TextView pageNumber;
    private boolean seekbarHidden=false;

    public ReaderFragment() {
        // Required empty public constructor
    }


    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
        DisplayMetrics metrics = Objects.requireNonNull(getContext()).getResources().getDisplayMetrics();
        pageBitmap = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.ARGB_8888);
        previewBitmap = Bitmap.createBitmap(metrics.widthPixels/4, metrics.heightPixels/4, Bitmap.Config.RGB_565);
        readerPage.setImageBitmap(pageBitmap);
        renderPage(pageBitmap);
        enableButton(pageBackward, false);
        seekBarDrawer = view.findViewById(R.id.seekBarDrawer);
        seekBarDrawer.setOnClickListener(view1 -> {
            if(seekbarHidden) {
                setSeekbarVisibility(true);
            }
        });
        readerPage.setOnClickListener(view12 -> {
            if(!seekbarHidden){
                setSeekbarVisibility(false);
            }
        });
        seekBar = view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    setCurrentPage(i);
                    renderPage(previewBitmap);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                syncButtons(currentPage);
                renderPage(pageBitmap);
            }
        });
        pageNumber = view.findViewById(R.id.pageNumber);
        setCurrentPage(0);
        seekBar.setMax(pageCount);
        return view;
    }

    private void setSeekbarVisibility(boolean visible){
        for(int i = 0; i < seekBarDrawer.getChildCount(); i++){
            seekBarDrawer.getChildAt(i).setVisibility(visible?View.VISIBLE:View.INVISIBLE);
        }
        seekBarDrawer.setAlpha(visible?1f:0);
        seekbarHidden = !visible;
    }

    /** Turn page is either called by pressing the floating buttons or using VOL+/-.
     *
     * @param forward page turn direction
     */
    void turnPage(boolean forward) {
        int idx = currentPage+(forward?(currentPage==pageCount?0:1):(currentPage==0?0:-1));
        syncButtons(idx);
        renderPage(pageBitmap);
    }

    private void syncButtons(int idx){
        enableButton(pageForward, true);
        enableButton(pageBackward, true);
        if(idx==pageCount)
            enableButton(pageForward, false);
        else if(idx==0)
            enableButton(pageBackward, false);
        setCurrentPage(idx);
    }

    private void setCurrentPage(int idx){
        currentPage=idx;
        pageNumber.setText(getString(R.string.page_number, idx, pageCount));
    }

    /** Used to change the opacity of the floating buttons and enable/disable them.
     *
     * @param b button
     * @param enable whether to enable or disable
     */
    private void enableButton(FloatingActionButton b, boolean enable) {
        b.setEnabled(enable);
        b.setAlpha(enable ? 1.0f : 0.5f);
    }

    /** wrapper for multiple calls to render a page on the canvas.
     *
     */
    private void renderPage(Bitmap bitmap) {
        if (renderer != null) {
            PdfRenderer.Page page = renderer.openPage(currentPage);
            page.render(bitmap);
            readerPage.setImageBitmap(bitmap);
            page.close();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        String uriString = Objects.requireNonNull(getArguments()).getString(DocumentExplorerFragment.EXAMDOCUMENT_FIELD);
        ParcelFileDescriptor fileDescriptor;
        try {
            fileDescriptor = getContext().getContentResolver().openFileDescriptor(Uri.parse(uriString), "r");
            renderer = new PdfRenderer(context,fileDescriptor);
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
