package edu.hm.eem_host.view;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import edu.hm.eem_host.R;
import edu.hm.eem_library.model.TeacherExam;
import edu.hm.eem_library.model.TeacherExamDocumentItemViewModel;
import edu.hm.eem_library.model.ThumbnailedExamDocument;
import edu.hm.eem_library.view.AbstractExamEditorActivity;

public class ExamEditorActivity extends AbstractExamEditorActivity {

    private EditText pwField;
    private CheckBox allDocAllowedField;
    private boolean allowAnnotations;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(TeacherExamDocumentItemViewModel.class);
        setContentView(R.layout.activity_exam_editor);
        pwField = findViewById(R.id.pass);
        allDocAllowedField = findViewById(R.id.allDocsAllowed);
        fileCounter = findViewById(R.id.used_files);
        svButton = findViewById(R.id.bt_save);
        delButton = findViewById(R.id.bt_del_doc);
        addButton = findViewById(R.id.bt_add_doc);
        progressBar = findViewById(R.id.progress);
        addButton.setOnClickListener(v -> showSourceDialog());
        ((CheckBox)findViewById(R.id.showPass)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) pwField.setTransformationMethod(null);
            else pwField.setTransformationMethod(new PasswordTransformationMethod());
        });
        docList = findViewById(R.id.doc_list);
        allDocAllowedField.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                if(!model.getLivedata().isEmpty())
                    showRemoveAllDocsDialog();
                else docUISetEnabled(false);
            } else {
                docUISetEnabled(true);
            }
        });
        toolbar = findViewById(R.id.toolbar);
    }

    @Override
    protected void progress(boolean on, boolean hideList) {
        progressBar.setVisibility(on?View.VISIBLE:View.GONE);
        if(hideList) docList.setVisibility(on?View.INVISIBLE:View.VISIBLE);
    }

    private void docUISetEnabled(boolean enable){
        enableButton(addButton,enable);
        docList.setAlpha(enable?1.0f:0.5f);
    }

    private void showRemoveAllDocsDialog(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_delete_all_documents));
        builder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
            model.getLivedata().clean(false);
            docUISetEnabled(false);
        });
        builder.setNegativeButton(getString(android.R.string.no), (dialog, which) -> {
            allDocAllowedField.setChecked(false);
            dialog.cancel();
        });
        builder.show();
    }

    @Override
    public void onClick(View v) {
        String pw = pwField.getText().toString();
        if(pw.isEmpty()){
            Toast.makeText(getApplicationContext(),getString(R.string.toast_enter_password),Toast.LENGTH_SHORT).show();
            return;
        }
        if(model.getLivedata().isEmpty() && !allDocAllowedField.isChecked()){
            Toast.makeText(getApplicationContext(),getString(R.string.toast_select_documents_teacher),Toast.LENGTH_SHORT).show();
            return;
        }
        ((TeacherExam)model.getCurrent()).setPassword(pw);
        super.onClick(v);
    }

    private void showSourceDialog(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_build_document));
        View v = getLayoutInflater().inflate(R.layout.dialog_build_examdocument, null);
        builder.setView(v);
        EditText numPages = v.findViewById(R.id.number_pages);
        RadioGroup rg = v.findViewById(R.id.rbs);
        CheckBox allowCb = v.findViewById(R.id.allow_annotations);
        builder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
            switch(rg.getCheckedRadioButtonId()){
                case R.id.rb_file:
                    allowAnnotations = allowCb.isChecked();
                    checkFileManagerPermissions();
                    break;
                case R.id.rb_page:
                    int pages;
                    try {
                        pages = Integer.parseInt(numPages.getText().toString());
                    } catch (NumberFormatException e){
                        Toast.makeText(getApplicationContext(), R.string.toast_specify_pages, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ThumbnailedExamDocument doc = ThumbnailedExamDocument.getInstance(getApplicationContext(), pages);
                    model.getLivedata().add(doc, false);
                    break;
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void handleDocument(@Nullable Uri uri) {
        if(uri!=null) {
            ThumbnailedExamDocument examDocument = ThumbnailedExamDocument.getInstance(this, uri);
            if (allowAnnotations)
                examDocument.item.removeHash();
            else
                examDocument.item.removeNonAnnotatedHash();
            model.getLivedata().add(examDocument, false);
            updateFileCounter();
        }
    }
}
