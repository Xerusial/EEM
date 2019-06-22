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

public class ExamEditorActivity extends AbstractExamEditorActivity {

    private ImageView progressBg;
    private ImageView progress;
    private AnimationDrawable progressAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(StudentExamDocumentItemViewModel.class);
        setContentView(R.layout.activity_exam_editor);
        fileCounter = findViewById(R.id.used_files);
        svButton = findViewById(R.id.bt_save);
        delButton = findViewById(R.id.bt_del_doc);
        addButton = findViewById(R.id.bt_add_doc);
        addButton.setOnClickListener(v -> checkFileManagerPermissions());
        toolbar = findViewById(R.id.toolbar);
        progressBg = findViewById(R.id.progress_background);
        progress = findViewById(R.id.progress);
        progressAnim = (AnimationDrawable) progress.getDrawable();
        docList = findViewById(R.id.doc_list);
    }

    @Override
    public void onClick(View v) {
        if(model.getLivedata().isEmpty()){
            Toast.makeText(getApplicationContext(), R.string.toast_select_documents_student, Toast.LENGTH_SHORT).show();
        }
        super.onClick(v);
    }

    @Override
    protected void progress(boolean on, boolean hideList){
        if(on){
            progressAnim.start();
        } else {
            progressAnim.stop();
        }
        progressBg.setVisibility(on?View.VISIBLE:View.GONE);
        progress.setVisibility(on?View.VISIBLE:View.GONE);
        if(hideList) docList.setVisibility(on?View.INVISIBLE:View.VISIBLE);
    }
}
