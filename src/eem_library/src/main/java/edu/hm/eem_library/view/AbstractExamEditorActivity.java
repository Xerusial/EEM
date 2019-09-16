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

/**
 * Core class for both exam editor activities of host and client, as they have many common features
 */
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
    private String examName;

    /**
     * Init PDFBox loader for faster document hashing
     *
     * @param savedInstanceState Android basics
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PDFBoxResourceLoader.init(getApplicationContext());
        examName = getIntent().getStringExtra(AbstractMainActivity.EXAMNAME_FIELD);
    }

    /**
     * Views are initialized in the child classes, so onCreate is moved to onPostCreate
     * Here all UI update hooks are set and views are configured
     *
     * @param savedInstanceState Android basics
     */
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

    /**
     * Show a progress bar and hide the exams list
     *
     * @param on       Progress is running
     * @param hideList Hide the list of documents at the same time
     */
    protected void progress(boolean on, boolean hideList) {
        progress.setVisibility(on ? View.VISIBLE : View.GONE);
        docList.setVisibility(hideList ? View.INVISIBLE : View.VISIBLE);
    }

    /**
     * CLick listener for the save button
     *
     * @param v view that was clicked on
     */
    @Override
    public void onClick(View v) {
        model.closeExam();
        finish();
    }

    /**
     * Disable buttons and make them opaque
     *
     * @param b      target button
     * @param enable enable/ disable it
     */
    protected void enableButton(ImageButton b, boolean enable) {
        b.setEnabled(enable);
        b.setAlpha(enable ? 1.0f : 0.5f);
    }

    /**
     * Empty callback if a press on a document item has occurred
     *
     * @param index index of the document
     */
    @Override
    public void onListFragmentPress(int index) {

    }

    /**
     * If a document has been returned, load it using a async task
     *
     * @param uri returned uri from the file manager
     */
    protected void handleDocument(@Nullable Uri uri) {
        if (uri != null) {
            SingleDocumentLoader loader = new SingleDocumentLoader(this);
            loader.execute(uri);
        }
    }

    /**
     * Refresh the persistent URI counter. Only 128 are allowed per Android app, so display to the
     * user if we get close to the limit
     */
    private void updateFileCounter() {
        int cnt = getContentResolver().getPersistedUriPermissions().size();
        fileCounter.setText(getString(R.string.used_files, cnt));
    }

    /**
     * A tutorial using a showcase library
     */
    protected void tutorial() {
        ShowcaseConfig config = new ShowcaseConfig();

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(addButton,
                getString(R.string.tutorial_adddoc_button), getString(android.R.string.ok));

        sequence.addSequenceItem(delButton,
                getString(R.string.tutorial_deldoc_button), getString(android.R.string.ok));

        sequence.addSequenceItem(TransformedShowCaseView.getInstance(this, svButton, R.string.tutorial_sv_button, 0,0,200));

        sequence.addSequenceItem(fileCounter,
                getString(R.string.tutorial_file_counter), getString(android.R.string.ok));
        sequence.addSequenceItem(fileCounter,
                getString(R.string.tutorial_file_counter2), getString(android.R.string.ok));

        sequence.start();
    }

    /**
     * An async task being responsible for loading multiple document thumbnails (after the activity
     * was started)
     */
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

    /**
     * Load a single document thumbnail & hash (in the process of editing the exam)
     */
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

