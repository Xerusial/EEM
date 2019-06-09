package edu.hm.eem_host.view;

import android.Manifest;
import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import edu.hm.eem_host.R;
import edu.hm.eem_library.model.ExamDocument;
import edu.hm.eem_library.model.ExamViewModel;
import edu.hm.eem_library.model.HASHTOOLBOX;
import edu.hm.eem_library.view.ItemListFragment;

public class ExamEditorActivity extends AppCompatActivity implements View.OnClickListener, ItemListFragment.OnListFragmentPressListener{

    private ExamViewModel model;

    private EditText pwField;
    private CheckBox allDocAllowedField;
    private ImageButton delButton;
    private ImageButton addButton;
    private View docList;

    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private final int READ_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(ExamViewModel.class);
        String examName = getIntent().getStringExtra("Name");
        model.openExam(examName);
        setContentView(R.layout.activity_exam_editor);
        pwField = findViewById(R.id.pass);
        allDocAllowedField = findViewById(R.id.allDocsAllowed);
        delButton = findViewById(R.id.bt_del_doc);
        addButton = findViewById(R.id.bt_add_doc);
        addButton.setOnClickListener(v -> showNameDialog());
        delButton.setOnClickListener(v -> model.getLivedata().removeSelected());
        findViewById(R.id.bt_save).setOnClickListener(this);
        ((CheckBox)findViewById(R.id.showPass)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) pwField.setTransformationMethod(null);
            else pwField.setTransformationMethod(new PasswordTransformationMethod());
        });
        docList = findViewById(R.id.doc_list);
        allDocAllowedField.setOnCheckedChangeListener((buttonView, isChecked) -> {
            docUISetEnabled(!isChecked);
        });
        allDocAllowedField.setChecked(model.getCurrent().allDocumentsAllowed);
        ((Toolbar)findViewById(R.id.toolbar2)).setTitle(examName);
    }

    private void setFields(){
        model.getCurrent().allDocumentsAllowed = allDocAllowedField.isChecked();
    }

    private void docUISetEnabled(boolean enable){
        delButton.setEnabled(enable);
        addButton.setEnabled(enable);
        delButton.setAlpha(enable?1.0f:0.5f);
        addButton.setAlpha(enable?1.0f:0.5f);
        docList.setAlpha(enable?1.0f:0.5f);
        docList.setEnabled(enable);
    }

    @Override
    public void onClick(View v) {
        String pw = pwField.getText().toString();
        if(pw.isEmpty()){
            Toast.makeText(getApplicationContext(),getString(R.string.toast_enter_password),Toast.LENGTH_SHORT).show();
            return;
        }
        if(model.getLivedata().getValue().size() == 0 && !allDocAllowedField.isChecked()){
            Toast.makeText(getApplicationContext(),getString(R.string.toast_select_documents),Toast.LENGTH_SHORT).show();
            return;
        }
        model.getCurrent().setPassword(pw);
        setFields();
        model.writeExamToFile();
        finish();
    }

    private void showNameDialog(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_build_document));
        View v = getLayoutInflater().inflate(R.layout.dialog_build_examdocument, null);
        builder.setView(v);
        EditText numPages = v.findViewById(R.id.number_pages);
        RadioGroup rg = v.findViewById(R.id.rbs);
        builder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
            switch(rg.getCheckedRadioButtonId()){
                case R.id.rb_file:
                    checkFileManagerPermissions();
                    break;
                case R.id.rb_page:
                    int pages = Integer.parseInt(numPages.getText().toString());
                    String fileName = getString(R.string.page_specified_document);
                    ExamDocument examDocument = new ExamDocument(fileName,pages);
                    model.getLivedata().add(fileName,examDocument, false);
                    break;
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onListFragmentPress(int index) {

    }

    @Override
    public void onListFragmentLongPress() {
        int sel_cnt = model.getLivedata().getSelectionCount();
        delButton.setEnabled(sel_cnt>0);
    }

    private void openFileManager(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void checkFileManagerPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
                Toast.makeText(getApplicationContext(),getString(R.string.toast_read_files_warning), Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            openFileManager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openFileManager();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                Uri uri = resultData.getData();
                byte[] hash = new byte[0];
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    hash = HASHTOOLBOX.genMD5(is);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String fileName = (new File(uri.getPath())).getName();
                ExamDocument examDocument = new ExamDocument(fileName, hash);
                model.getLivedata().add(fileName,examDocument, false);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, resultData);
        }
    }



}
