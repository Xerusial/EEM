package edu.hm.eem_library.view;

import androidx.annotation.Nullable;

import android.net.Uri;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toolbar;

import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.lang.ref.WeakReference;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ExamDocumentItemViewModel;
import edu.hm.eem_library.model.HASHTOOLBOX;
import edu.hm.eem_library.model.StudentExam;
import edu.hm.eem_library.model.ThumbnailedExamDocument;

public abstract class AbstractExamEditorActivity extends DocumentPickerActivity implements View.OnClickListener, ItemListFragment.OnListFragmentPressListener {

    protected ExamDocumentItemViewModel<? extends StudentExam> model;

    protected ImageButton addButton;
    protected ImageButton delButton;
    protected Button svButton;
    protected Toolbar toolbar;
    protected View docList;

    protected TextView fileCounter;

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
        updateFileCounter();
        DocumentLoader loader = new DocumentLoader(this, examName);
        loader.execute();
    }

    static class DocumentLoader extends AsyncTask<Void, Void, Void> {
        private final WeakReference<AbstractExamEditorActivity> context;
        private final String examName;

        public DocumentLoader(AbstractExamEditorActivity context, String examName) {
            this.context = new WeakReference<>(context);
            this.examName = examName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context.get().progress(true, true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            context.get().model.openExam(examName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            context.get().progress(false, true);
        }
    }

    protected abstract void progress(boolean on, boolean hideList);

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
            SingleDocumentLoader loader = new SingleDocumentLoader(this);
            loader.execute(uri);
        }
    }

    protected static class SingleDocumentLoader extends AsyncTask<Uri, Void, ThumbnailedExamDocument> {
        protected final WeakReference<AbstractExamEditorActivity> context;

        protected SingleDocumentLoader(AbstractExamEditorActivity context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context.get().progress(true, false);
        }

        @Override
        protected ThumbnailedExamDocument doInBackground(Uri... uris) {
            ThumbnailedExamDocument examDocument = ThumbnailedExamDocument.getInstance(context.get(), uris[0], HASHTOOLBOX.WhichHash.BOTH);
            return examDocument;
        }

        @Override
        protected void onPostExecute(ThumbnailedExamDocument doc) {
            super.onPostExecute(doc);
            context.get().progress(false, false);
            context.get().model.getLivedata().add(doc, false);
            context.get().updateFileCounter();
        }
    }

    protected final void updateFileCounter(){
        int cnt = getContentResolver().getPersistedUriPermissions().size();
        fileCounter.setText(getString(R.string.used_files, cnt));
    }
}

