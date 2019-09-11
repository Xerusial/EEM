package edu.hm.eem_library.view;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.lang.ref.WeakReference;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ExamDocumentItemViewModel;
import edu.hm.eem_library.model.HASHTOOLBOX;
import edu.hm.eem_library.model.StudentExam;
import edu.hm.eem_library.model.ThumbnailedExamDocument;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public abstract class AbstractExamEditorActivity extends DocumentPickerActivity implements View.OnClickListener, ItemListFragment.OnListFragmentPressListener {

    private static final String SHOWCASE_ID = "ExamEditorActivity";
    protected ExamDocumentItemViewModel<? extends StudentExam> model;
    protected ImageButton addButton;
    protected ImageButton delButton;
    protected Button svButton;
    protected Toolbar toolbar;
    protected View docList;
    protected ConstraintLayout progress;
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
            updateFileCounter();
        });
        svButton.setOnClickListener(this);
        toolbar.setTitle(examName);
        enableButton(delButton, false);
        updateFileCounter();
        DocumentLoader loader = new DocumentLoader(this, examName);
        loader.execute();
        tutorial();
    }

    protected void progress(boolean on, boolean hideList){
        progress.setVisibility(on ? View.VISIBLE : View.GONE);
        docList.setVisibility(hideList ? View.INVISIBLE : View.VISIBLE);
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

    protected void handleDocument(@Nullable Uri uri) {
        if (uri != null) {
            SingleDocumentLoader loader = new SingleDocumentLoader(this);
            loader.execute(uri);
        }
    }

    protected final void updateFileCounter() {
        int cnt = getContentResolver().getPersistedUriPermissions().size();
        fileCounter.setText(getString(R.string.used_files, cnt));
    }

    protected void tutorial() {
        ShowcaseConfig config = new ShowcaseConfig();

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(addButton,
                getString(R.string.tutorial_adddoc_button), getString(android.R.string.ok));

        sequence.addSequenceItem(delButton,
                getString(R.string.tutorial_deldoc_button), getString(android.R.string.ok));

        sequence.addSequenceItem(svButton,
                getString(R.string.tutorial_sv_button), getString(android.R.string.ok));

        sequence.addSequenceItem(fileCounter,
                getString(R.string.tutorial_file_counter), getString(android.R.string.ok));
        sequence.addSequenceItem(fileCounter,
                getString(R.string.tutorial_file_counter2), getString(android.R.string.ok));

        sequence.start();
    }

    static class DocumentLoader extends AsyncTask<Void, Void, Void> {
        private final WeakReference<AbstractExamEditorActivity> context;
        private final String examName;

        DocumentLoader(AbstractExamEditorActivity context, String examName) {
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
            context.get().progress(false, false);
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
            context.get().progress(true, true);
        }

        @Override
        protected ThumbnailedExamDocument doInBackground(Uri... uris) {
            return ThumbnailedExamDocument.getInstance(context.get(), uris[0], HASHTOOLBOX.WhichHash.BOTH);
        }

        @Override
        protected void onPostExecute(ThumbnailedExamDocument doc) {
            super.onPostExecute(doc);
            context.get().progress(false, false);
            context.get().model.getLivedata().add(doc, false);
        }
    }
}

