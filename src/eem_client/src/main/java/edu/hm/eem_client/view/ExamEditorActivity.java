package edu.hm.eem_client.view;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.StudentExamDocumentItemViewModel;
import edu.hm.eem_library.view.AbstractExamEditorActivity;

/**
 * The implementation of the {@link AbstractExamEditorActivity} for the client side.
 * This class is only used to change the layout and set the progress symbol to one
 * that is optimized for E-Ink screens
 */
public class ExamEditorActivity extends AbstractExamEditorActivity {

    private AnimationDrawable progressAnim;

    /**
     * Get a viewmodel for our list, init views, and set clicklisteners
     *
     * @param savedInstanceState Android basics
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(StudentExamDocumentItemViewModel.class);
        setContentView(R.layout.activity_exam_editor);
        fileCounter = findViewById(R.id.used_files);
        svButton = findViewById(R.id.bt_save);
        delButton = findViewById(R.id.bt_del_doc);
        addButton = findViewById(R.id.bt_add_doc);
        toolbar = findViewById(R.id.toolbar);
        progress = findViewById(R.id.progress);
        ImageView progressDots = findViewById(R.id.progressAnim);
        docList = findViewById(R.id.doc_list);
        addButton.setOnClickListener(v -> checkFileManagerPermissions());
        progressAnim = (AnimationDrawable) progressDots.getDrawable();
    }

    /**
     * The save button onClickListener. Make sure at least one document is specified
     *
     * @param v view that was clicked on
     */
    @Override
    public void onClick(View v) {
        if (model.getLivedata().isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.toast_select_documents_student, Toast.LENGTH_SHORT).show();
        }
        super.onClick(v);
    }

    /**
     * Setting custom progress symbol
     *
     * @param on       Progress is running
     * @param hideList Hide the list of documents at the same time
     */
    @Override
    protected void progress(boolean on, boolean hideList) {
        if (on) {
            progressAnim.start();
        } else {
            progressAnim.stop();
        }
        super.progress(on, hideList);
    }
}
