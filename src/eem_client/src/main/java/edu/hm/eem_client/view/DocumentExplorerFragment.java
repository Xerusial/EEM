package edu.hm.eem_client.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.Objects;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.StudentExamDocumentItemViewModel;
import edu.hm.eem_library.model.ThumbnailedExamDocument;
import edu.hm.eem_library.view.AbstractMainActivity;
import edu.hm.eem_library.view.ItemListFragment;

/**
 * One of the two {@link Fragment}s held by the navhost in the {@link LockedActivity}. It is used
 * to display the selection of documents to the student.
 */
public class DocumentExplorerFragment extends Fragment implements ItemListFragment.OnListFragmentPressListener {
    final static String EXAMDOCUMENT_FIELD = "ExamDocument";

    private StudentExamDocumentItemViewModel model;
    private OnDocumentsAcceptedListener listener;

    public DocumentExplorerFragment() {
        // Required empty public constructor
    }

    /**
     * Init all views. Get exam title and prof name from args.
     *
     * @param inflater           Android basics
     * @param container          Android basics
     * @param savedInstanceState Android basics
     * @return the created View
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_document_explorer, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        Bundle args = getArguments();
        String examName = Objects.requireNonNull(args).getString(AbstractMainActivity.EXAMNAME_FIELD);
        String profName = args.getString(ScanActivity.PROF_FIELD);
        toolbar.setTitle(examName + " @ " + profName);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(StudentExamDocumentItemViewModel.class);
        if (context instanceof OnDocumentsAcceptedListener) {
            listener = (OnDocumentsAcceptedListener) context;
        }
    }

    @Override
    public void onListFragmentPress(int index) {
        @SuppressWarnings("unchecked")
        ArrayList<ThumbnailedExamDocument> list = (ArrayList<ThumbnailedExamDocument>) model.getLivedata().getValue();
        ThumbnailedExamDocument doc = Objects.requireNonNull(list).get(index);
        if (doc.selected) {
            showPwDialog(doc);
        } else {
            Bundle bundle = new Bundle();
            String path = doc.item.getUriString();
            bundle.putString(EXAMDOCUMENT_FIELD, path);
            //noinspection ResultOfMethodCallIgnored
            Navigation.createNavigateOnClickListener(R.id.action_open_reader, bundle);
            Navigation.findNavController(Objects.requireNonNull(getView())).navigate(R.id.action_open_reader, bundle);
        }
    }

    /**
     * A dialog which allows the teacher to accept documents that where not correctly identified
     * by the algorithm.
     *
     * @param doc The document, that was touched to be accepted
     */
    private void showPwDialog(ThumbnailedExamDocument doc) {
        TextView textView = new TextView(getContext());
        textView.setText(getString(R.string.dialog_explorer));
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(20, 20, 20, 0);
        textView.setPadding(20, 20, 20, 0);
        textView.setLayoutParams(lp);

        final EditText input = new EditText(getContext());

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCustomTitle(textView)
                .setView(input)

                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String text = input.getText().toString();
                    try {
                        if (model.teacherExam.checkPW(text)) {
                            model.getLivedata().clearSelection(doc.item.getName());
                            listener.onDocumentsAccepted();
                        } else
                            Toast.makeText(getContext(), R.string.toast_wrong_password, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                })
                .setNeutralButton(R.string.preview_document, (dialog, which) -> {
                    Bundle bundle = new Bundle();
                    String uriString = doc.item.getUriString();
                    bundle.putString(EXAMDOCUMENT_FIELD, uriString);
                    //noinspection ResultOfMethodCallIgnored
                    Navigation.createNavigateOnClickListener(R.id.action_open_reader, bundle);
                    Navigation.findNavController(Objects.requireNonNull(getView())).navigate(R.id.action_open_reader, bundle);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    /**
     * Callback for whenever a document has been accepted
     */
    public interface OnDocumentsAcceptedListener {
        void onDocumentsAccepted();
    }
}
