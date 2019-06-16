package edu.hm.eem_client.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import java.util.ArrayList;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.ExamViewModel;
import edu.hm.eem_library.model.StudentExamViewModel;
import edu.hm.eem_library.model.ThumbnailedExamDocument;
import edu.hm.eem_library.view.AbstractMainActivity;
import edu.hm.eem_library.view.ItemListFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DocumentExplorerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class DocumentExplorerFragment extends Fragment implements ItemListFragment.OnListFragmentPressListener {
    public final static String EXAMDOCUMENT_FIELD = "ExamDocument";

    private OnFragmentInteractionListener mListener;
    private StudentExamViewModel model;
    private Toolbar toolbar;

    public DocumentExplorerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_document_explorer, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        Bundle args = getArguments();
        String examName = args.getString(AbstractMainActivity.EXAMNAME_FIELD);
        String profName = args.getString(ScanActivity.PROF_FIELD);
        toolbar.setTitle(examName + " @ " + profName);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
        model = ViewModelProviders.of(getActivity()).get(StudentExamViewModel.class);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListFragmentPress(int index) {
        Bundle bundle = new Bundle();
        String path = ((ArrayList<ThumbnailedExamDocument>) model.getLivedata().getValue()).get(index).item.getPath();
        bundle.putString(EXAMDOCUMENT_FIELD, path);
        Navigation.createNavigateOnClickListener(R.id.action_open_reader, bundle);
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
