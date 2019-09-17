package edu.hm.eem_client.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Objects;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.PdfRenderer;

/**
 * Fragment used to display the PDFs in the exam.
 */
public class ReaderFragment extends Fragment {

    private FloatingActionButton pageForward, pageBackward;
    private PdfRenderer renderer = null;
    private int pageCount, currentPage;
    private Bitmap pageBitmap, previewBitmap;
    private ImageView readerPage;
    private ConstraintLayout seekBarDrawer;
    private TextView pageNumber;
    private boolean seekbarHidden = false;
    ScaleGestureDetector scaleGestureDetector;
    private float scale = 1;
    private int x = 0 ,y = 0, maxX, maxY;

    public ReaderFragment() {
        // Required empty public constructor
    }

    /**
     * Init views, set clock listeners for page turn buttons and seek slider
     *
     * @param inflater           Android basics
     * @param container          Android basics
     * @param savedInstanceState Android basics
     * @return The created view
     */
    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reader, container, false);
        SeekBar seekBar = view.findViewById(R.id.seekBar);
        pageForward = view.findViewById(R.id.page_forward);
        pageBackward = view.findViewById(R.id.page_backwards);
        seekBarDrawer = view.findViewById(R.id.seekBarDrawer);
        readerPage = view.findViewById(R.id.reader_page);
        pageNumber = view.findViewById(R.id.pageNumber);
        if (pageCount > 1) {
            pageForward.setOnClickListener(v -> turnPage(true));
            pageBackward.setOnClickListener(v -> turnPage(false));
        } else {
            pageForward.hide();
            pageBackward.hide();
        }
        DisplayMetrics metrics = Objects.requireNonNull(getContext()).getResources().getDisplayMetrics();
        pageBitmap = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.RGB_565);
        previewBitmap = Bitmap.createBitmap(metrics.widthPixels / 4, metrics.heightPixels / 4, Bitmap.Config.RGB_565);
        readerPage.setImageBitmap(pageBitmap);
        setCurrentPage(0);
        seekBar.setMax(pageCount);
        renderPage(pageBitmap, scale, x, y);
        enableButton(pageBackward, false);
        seekBarDrawer.setOnClickListener(view1 -> {
            if (seekbarHidden) {
                setSeekbarVisibility(true);
            }
        });
        readerPage.setOnClickListener(view12 -> {
            if (!seekbarHidden) {
                setSeekbarVisibility(false);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    setCurrentPage(i);
                    renderPage(previewBitmap, scale, x, y);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                syncButtons(currentPage);
                renderPage(pageBitmap, scale, x, y);
            }
        });
        scaleGestureDetector = new ScaleGestureDetector(this.getContext(), new PDFTransformDetector());
        return view;
    }

    /**
     * A Detector for zooming an panning
     */
    private class PDFTransformDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private int startX, startY, cngX, cngY;

        // Detects that new pointers are going down.
        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            startX = (int) scaleGestureDetector.getFocusX();
            startY = (int) scaleGestureDetector.getFocusY();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            int maxVPX = (int) (maxX * (scale - 1));
            int maxVPY = (int) (maxY / 2 * (scale - 1));
            scale *= scaleGestureDetector.getScaleFactor();
            scale = Math.max(1f, Math.min(scale, 5.0f));
            cngX = (int) scaleGestureDetector.getFocusX() - startX + x;
            cngY = (int) scaleGestureDetector.getFocusY() - startY + y;
            cngX = cngX < -maxVPX ? -maxVPX : cngX > 0 ? 0 : cngX;
            cngY = cngY < -maxVPY ? -maxVPY : cngY > maxVPY ? maxVPY : cngY;
            ReaderFragment.this.renderPage(pageBitmap, scale, cngX, cngY);
            ViewCompat.postInvalidateOnAnimation(readerPage);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            x = cngX;
            y = cngY;
            super.onScaleEnd(detector);
        }
    }

    /**
     * Sets the seekslider visibility
     *
     * @param visible?
     */
    private void setSeekbarVisibility(boolean visible) {
        for (int i = 0; i < seekBarDrawer.getChildCount(); i++) {
            seekBarDrawer.getChildAt(i).setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
        seekBarDrawer.setAlpha(visible ? 1f : 0);
        seekbarHidden = !visible;
    }

    /**
     * Turn page is either called by pressing the floating buttons or using VOL+/-.
     *
     * @param forward page turn direction
     */
    void turnPage(boolean forward) {
        int idx = currentPage + (forward ? (currentPage == pageCount ? 0 : 1) : (currentPage == 0 ? 0 : -1));
        syncButtons(idx);
        renderPage(pageBitmap, scale, x, y);
    }

    /**
     * Disables onScreen buttons, when end/beginning of document is reached
     *
     * @param idx updated page index
     */
    private void syncButtons(int idx) {
        enableButton(pageForward, true);
        enableButton(pageBackward, true);
        if (idx == pageCount)
            enableButton(pageForward, false);
        else if (idx == 0)
            enableButton(pageBackward, false);
        setCurrentPage(idx);
        scale = 1;
        x = y = 0;
    }

    /**
     * Sets the current page while also updating the page indicator
     *
     * @param idx value to set to
     */
    private void setCurrentPage(int idx) {
        currentPage = idx;
        pageNumber.setText(getString(R.string.page_number, idx, pageCount));
    }

    /**
     * Used to change the opacity of the floating buttons and enable/disable them.
     *
     * @param b      button
     * @param enable whether to enable or disable
     */
    private void enableButton(FloatingActionButton b, boolean enable) {
        b.setEnabled(enable);
        b.setAlpha(enable ? 1.0f : 0.5f);
    }

    /**
     * wrapper for multiple calls to render a page on the canvas.
     *
     * @param bitmap render to highRes or preview Bitmap?
     */
    private void renderPage(Bitmap bitmap, float scale, int x, int y) {
        if (renderer != null) {
            PdfRenderer.Page page = renderer.openPage(currentPage);
            page.render(bitmap, x, y, scale);
            readerPage.setImageBitmap(bitmap);
            page.close();
        }
    }

    /**
     * Getting PDF file and meta before setting up the rest of the UI
     *
     * @param context parent App/Activity
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        String uriString = Objects.requireNonNull(getArguments()).getString(DocumentExplorerFragment.EXAMDOCUMENT_FIELD);
        ParcelFileDescriptor fileDescriptor;
        try {
            fileDescriptor = Objects.requireNonNull(getContext()).getContentResolver().openFileDescriptor(Uri.parse(uriString), "r");
            renderer = new PdfRenderer(context, fileDescriptor);
            pageCount = renderer.getPageCount() - 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        maxX = size.x;
        maxY = size.y;
    }

    /**
     * Close renderer when finished
     */
    @Override
    public void onDetach() {
        super.onDetach();
        if (renderer != null) renderer.close();
    }
}
