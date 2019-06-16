package edu.hm.eem_client.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.ParcelFileDescriptor;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.hm.eem_client.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReaderFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ReaderFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private String path;
    private FloatingActionButton pageForward, pageBackward;
    private ImageView readerPage;
    private PdfRenderer renderer = null;
    private int pageCount, currentPage;
    private Bitmap pageBitmap;

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
        pageForward.setOnClickListener(v -> turnPage(true));
        pageBackward.setOnClickListener(v -> turnPage(false));
        pageBitmap = Bitmap.createBitmap(readerPage.getWidth(), readerPage.getHeight(), Bitmap.Config.ARGB_8888);
        return view;
    }

    void turnPage(boolean forward){
        boolean endNotReached = currentPage!=pageCount;
        boolean beginningNotReached = currentPage!=0;
        if(forward && endNotReached){
            currentPage++;
            renderPage();
            enableButton(pageForward, currentPage!=pageCount);
            enableButton(pageBackward, true);
        } else if (!forward && beginningNotReached) {
            currentPage--;
            renderPage();
            enableButton(pageBackward, currentPage!=0);
            enableButton(pageForward, true);
        }

    }

    private void enableButton(FloatingActionButton b, boolean enable) {
        b.setEnabled(enable);
        b.setAlpha(enable ? 1.0f : 0.5f);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private boolean renderPage(){
        boolean ret = false;
        if(renderer!=null){
            PdfRenderer.Page page = renderer.openPage(currentPage);
            page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            ret = true;
        }
        return ret;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        path = getArguments().getString(DocumentExplorerFragment.EXAMDOCUMENT_FIELD);
        File file = new File(path);
        ParcelFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(fileDescriptor);
            currentPage = 0;
            pageCount = renderer.getPageCount();
            renderPage();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(renderer!=null) renderer.close();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
