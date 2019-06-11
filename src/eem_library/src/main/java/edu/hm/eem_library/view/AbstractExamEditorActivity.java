package edu.hm.eem_library.view;

import android.Manifest;
import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ExamDocument;
import edu.hm.eem_library.model.ExamViewModel;
import edu.hm.eem_library.model.HASHTOOLBOX;
import edu.hm.eem_library.model.THUMBNAILTOOLBOX;
import edu.hm.eem_library.model.ThumbnailedExamDocument;

public abstract class AbstractExamEditorActivity extends AppCompatActivity implements View.OnClickListener, ItemListFragment.OnListFragmentPressListener{

    protected ExamViewModel model;

    protected ImageButton addButton;
    protected ImageButton delButton;
    protected Button svButton;
    protected Toolbar toolbar;

    private String examName;

    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private final int READ_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(ExamViewModel.class);
        examName = getIntent().getStringExtra("Name");
        model.openExam(examName);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delButton.setOnClickListener(v -> model.getLivedata().removeSelected());
        model.getLivedata().observe(this, sortableItems -> {
            int sel_cnt = model.getLivedata().getSelectionCount();
            enableButton(delButton,sel_cnt>0);
        });
        svButton.setOnClickListener(this);
        toolbar.setTitle(examName);
        enableButton(delButton,false);
    }

    @Override
    public void onClick(View v) {
        model.closeExam();
        finish();
    }

    protected void enableButton(ImageButton b, boolean enable){
        b.setEnabled(enable);
        b.setAlpha(enable?1.0f:0.5f);
    }

    @Override
    public void onListFragmentPress(int index) {

    }

    private void openFileManager(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    protected void checkFileManagerPermissions(){
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
                File documentFile = new File(uri.getPath());
                try {
                    FileInputStream fis = new FileInputStream(documentFile);
                    hash = HASHTOOLBOX.genMD5(fis);
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap thumbnail = THUMBNAILTOOLBOX.getThumbnailBitmap(documentFile, getApplication());
                ThumbnailedExamDocument examDocument = new ThumbnailedExamDocument(documentFile.getName(), new ExamDocument(documentFile.getName(), hash, documentFile.getPath()), thumbnail);
                model.getLivedata().add(documentFile.getName(),examDocument, false);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, resultData);
        }
    }
}

