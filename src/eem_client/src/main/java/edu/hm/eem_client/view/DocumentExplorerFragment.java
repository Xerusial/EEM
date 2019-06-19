package edu.hm.eem_client.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.ArrayList;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.StudentExamViewModel;
import edu.hm.eem_library.model.ThumbnailedExamDocument;
import edu.hm.eem_library.view.AbstractMainActivity;
import edu.hm.eem_library.view.ItemListFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocumentExplorerFragment extends Fragment implements ItemListFragment.OnListFragmentPressListener {
    final static String EXAMDOCUMENT_FIELD = "ExamDocument";

    private StudentExamViewModel model;
    private OnDocumentsAcceptedListener listener;

    public DocumentExplorerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_document_explorer, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        Bundle args = getArguments();
        String examName = args.getString(AbstractMainActivity.EXAMNAME_FIELD);
        String profName = args.getString(ScanActivity.PROF_FIELD);
        toolbar.setTitle(examName + " @ " + profName);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        model = ViewModelProviders.of(getActivity()).get(StudentExamViewModel.class);
        if(context instanceof OnDocumentsAcceptedListener){
            listener = (OnDocumentsAcceptedListener) context;
        }
    }


    @Override
    public void onListFragmentPress(int index) {
        ThumbnailedExamDocument doc = ((ArrayList<ThumbnailedExamDocument>) model.getLivedata().getValue()).get(index);
        if(doc.selected){
            showPwDialog(doc);
        } else {
            Bundle bundle = new Bundle();
            String path = doc.item.getUriString();
            bundle.putString(EXAMDOCUMENT_FIELD, path);
            Navigation.createNavigateOnClickListener(R.id.action_open_reader, bundle);
            Navigation.findNavController(getView()).navigate(R.id.action_open_reader, bundle);
        }
    }

    private void showPwDialog(ThumbnailedExamDocument doc){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getContext());
        TextView textView = new TextView(getContext());
        textView.setText(getString(R.string.dialog_explorer));
        builder.setCustomTitle(textView);

        final EditText input = new EditText(getContext());

        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String text = input.getText().toString();
            try {
                if (model.teacherExam.checkPW(text)) {
                    model.getLivedata().clearSelection();
                    listener.onDocumentsAccepted();
                } else
                    Toast.makeText(getContext(), R.string.toast_wrong_password, Toast.LENGTH_SHORT).show();
            } catch (Exception e){
                e.printStackTrace();
            }

        });
        builder.setNeutralButton(R.string.preview_document, ((dialog, which) -> {
            Bundle bundle = new Bundle();
            String path = doc.item.getUriString();
            bundle.putString(EXAMDOCUMENT_FIELD, path);
            Navigation.createNavigateOnClickListener(R.id.action_open_reader, bundle);
            Navigation.findNavController(getView()).navigate(R.id.action_open_reader, bundle);
        }));
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public interface OnDocumentsAcceptedListener{
        void onDocumentsAccepted();
    }
}
