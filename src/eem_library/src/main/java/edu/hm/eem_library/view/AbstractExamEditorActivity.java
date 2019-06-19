package edu.hm.eem_library.view;

import androidx.annotation.Nullable;
import android.net.Uri;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toolbar;

import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import edu.hm.eem_library.model.ExamViewModel;
import edu.hm.eem_library.model.StudentExam;
import edu.hm.eem_library.model.ThumbnailedExamDocument;

public abstract class AbstractExamEditorActivity extends DocumentPickerActivity implements View.OnClickListener, ItemListFragment.OnListFragmentPressListener {

    protected ExamViewModel<? extends StudentExam> model;

    protected ImageButton addButton;
    protected ImageButton delButton;
    protected Button svButton;
    protected Toolbar toolbar;

    protected String examName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PDFBoxResourceLoader.init(getApplicationContext());
        examName = getIntent().getStringExtra(AbstractMainActivity.EXAMNAME_FIELD);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delButton.setOnClickListener(v -> model.getLivedata().removeSelected());
        model.getLivedata().observe(this, sortableItems -> {
            int sel_cnt = model.getLivedata().getSelectionCount();
            enableButton(delButton, sel_cnt > 0);
        });
        svButton.setOnClickListener(this);
        toolbar.setTitle(examName);
        enableButton(delButton, false);
    }

    @Override
    public void onClick(View v) {
        model.closeExam();
        finish();
    }

    protected void enableButton(ImageButton b, boolean enable) {
        b.setEnabled(enable);
        b.setAlpha(enable ? 1.0f : 0.5f);
    }

    @Override
    public void onListFragmentPress(int index) {

    }

    protected void handleDocument(@Nullable Uri uri){
        if(uri!=null) {
            ThumbnailedExamDocument examDocument = ThumbnailedExamDocument.getInstance(this, uri);
            model.getLivedata().add(examDocument, false);
        }
    }
}

