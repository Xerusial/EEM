package edu.hm.eem_library.view;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProviders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ExamDocument;
import edu.hm.eem_library.model.ExamFactory;
import edu.hm.eem_library.model.ExamItemViewModel;
import edu.hm.eem_library.model.HASHTOOLBOX;
import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.model.StudentExam;
import edu.hm.eem_library.model.ThumbnailedExamDocument;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Core class for client- and host main activities as they are virtually the same
 */
public abstract class AbstractMainActivity extends DocumentPickerActivity implements ItemListFragment.OnListFragmentPressListener {

    public static final String EXAMNAME_FIELD = "ExamName";
    private static final String SHOWCASE_ID = "MainActivity";

    protected ExamFactory.ExamType examType;
    private ExamItemViewModel model;
    private ImageButton add_button, del_button, edit_button;
    private TreeMap<String, Pair<Boolean, List<String>>> uriMap;
    private String replacementUri;
    private boolean doNotRebuildUriMap = false;

    /**
     * Init views, set click callbacks and UI update hooks
     *
     * @param savedInstanceState Android basics
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abstract_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.burger_popup);
        setActionBar(toolbar);
        model = ViewModelProviders.of(this).get(ExamItemViewModel.class);
        del_button = findViewById(R.id.bt_del_exam);
        del_button.setOnClickListener(v -> removeSelected());
        edit_button = findViewById(R.id.bt_edit_exam);
        edit_button.setOnClickListener(v -> {
            String name = Objects.requireNonNull(model.getLivedata().getSelected()).getName();
            startSubApplication(name, ActionType.ACTION_EDITOR);
        });
        add_button = findViewById(R.id.bt_add_exam);
        add_button.setOnClickListener(v -> {
            showNameDialog();
            //show keyboard
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
        });
        model.getLivedata().observe(this, sortableItems -> {
            int sel_cnt = model.getLivedata().getSelectionCount();
            buttonSetEnabled(del_button, sel_cnt > 0);
            buttonSetEnabled(edit_button, sel_cnt == 1);
        });
        tutorial();
    }

    /**
     * Burger popup for settings an about
     *
     * @param menu target menu
     * @return return value from passed on super class
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.burger_popup, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Generate a map of all persistent uris in all exams and delete the ones that are not
     * needed any more. As we only can have 128 at the same time, this is necessary housekeeping.
     * And we can't do this in exam editor, because if the user decides to delete document A from
     * the current exam, we cannot delete the uri as we do not know if a different exam also needs
     * the uri. The uri map is also needed to check if all documents are still there or if one needs
     * to be updated location- and hashwise.
     */
    private void buildUriMap() {
        ExamFactory factory = new ExamFactory(examType);
        ContentResolver resolver = getContentResolver();
        uriMap = new TreeMap<>();
        for (SelectableSortableItem<File> container : Objects.requireNonNull(model.getLivedata().getValue())) {
            try {
                FileInputStream fis = new FileInputStream(container.item);
                StudentExam exam = factory.extract(fis);
                fis.close();
                for (ExamDocument doc : exam.getAllowedDocuments()) {
                    String uriString = doc.getUriString();
                    if (uriString != null) {
                        if (!uriMap.containsKey(uriString)) {
                            try {
                                Uri uri = Uri.parse(uriString);
                                InputStream is = resolver.openInputStream(uri);
                                Objects.requireNonNull(is).close();
                                uriMap.put(uriString, Pair.create(true, new LinkedList<>()));
                            } catch (FileNotFoundException e) {
                                uriMap.put(uriString, Pair.create(false, new LinkedList<>()));
                            }
                        }
                        Pair<Boolean, List<String>> entry = uriMap.get(uriString);
                        Objects.requireNonNull(entry).second.add(container.item.getName());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        model.getLivedata().clearSelection();
        askToRemoveExams();
    }

    /**
     * Remove selected exams from list. Check if we can also delete unused Uris.
     */
    private void removeSelected() {
        if (model.getLivedata().getSelectionCount() != 0) {
            model.getLivedata().removeSelected();
        }
        List<UriPermission> list = getContentResolver().getPersistedUriPermissions();
        for (UriPermission perm : list) {
            Uri uri = perm.getUri();
            Pair<Boolean, List<String>> entry = uriMap.get(uri.toString());
            if (entry == null || entry.second.size() == 0)
                getContentResolver().releasePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    /**
     * Dialog asking the user what to do if a Uri points to a faulty location (Doc has been deleted)
     */
    private void askToRemoveExams() {
        boolean entryFound = false;
        for (Map.Entry<String, Pair<Boolean, List<String>>> entry : uriMap.entrySet()) {
            if (!entry.getValue().first) {
                // File was deleted: get new File
                Iterator<String> it = entry.getValue().second.iterator();
                StringBuilder sb = new StringBuilder(it.next());
                for (; it.hasNext(); ) {
                    sb.append(", ");
                    sb.append(it.next());
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(entry.getValue().second.size() > 1 ?
                                R.string.dialog_document_not_found_pl :
                                R.string.dialog_document_not_found_sing,
                        ThumbnailedExamDocument.getMetaFromUri(this, Uri.parse(entry.getKey())).name, sb.toString()))
                        .setPositiveButton(getString(R.string.dialog_document_not_found_bt_pos), (dialog, which) -> {
                            replacementUri = entry.getKey();
                            dialog.dismiss();
                            checkFileManagerPermissions();
                        })
                        .setNeutralButton(getString(R.string.dialog_document_not_found_bt_neutral), (dialog, which) -> {
                            getContentResolver().releasePersistableUriPermission(Uri.parse(entry.getKey()),
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            uriMap.remove(entry.getKey());
                            for (String s : entry.getValue().second)
                                model.getLivedata().setSelected(s, false);
                            dialog.dismiss();
                            askToRemoveExams();
                        })
                        .setNegativeButton(getString(R.string.dialog_document_not_found_bt_neg), (dialog, which) -> dialog.cancel())
                        .setOnCancelListener(dialog -> {
                            removeSelected();
                            finish();
                        })
                        .show();
                entryFound = true;
                break;
            }
        }
        //After all Exams have been checked, remove the ones being selected in the process
        if (!entryFound) removeSelected();
    }

    /**
     * If the user has decided to update the location of a missing document, update all
     * exams containing this document.
     *
     * @param uri selected uri returned by the file manager
     */
    @Override
    void handleDocument(@Nullable Uri uri) {
        if (uri != null) {
            Pair<Boolean, List<String>> entry = uriMap.get(replacementUri);
            ExamDocument doc = Objects.requireNonNull(ThumbnailedExamDocument.getInstance(this, uri, HASHTOOLBOX.WhichHash.BOTH)).item;
            for (String s : Objects.requireNonNull(entry).second) {
                ExamFactory factory = new ExamFactory(examType);
                for (SelectableSortableItem<File> container : Objects.requireNonNull(model.getLivedata().getValue())) {
                    if (container.item.getName().equals(s)) {
                        try {
                            FileInputStream fis = new FileInputStream(container.item);
                            StudentExam exam = factory.extract(fis);
                            fis.close();
                            List<ExamDocument> list = exam.getAllowedDocuments();
                            for (int i = 0; i < list.size(); i++) {
                                ExamDocument oldDoc = list.get(i);
                                if (Objects.equals(oldDoc.getUriString(), replacementUri)) {
                                    ExamDocument newDoc = (ExamDocument) doc.clone();
                                    if (oldDoc.getHash() == null)
                                        newDoc.removeHash();
                                    else
                                        newDoc.removeNonAnnotatedHash();
                                    list.set(i, newDoc);
                                }
                            }
                            factory.writeExamToFile(exam, container.item);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            uriMap.remove(replacementUri);
            getContentResolver().releasePersistableUriPermission(Uri.parse(replacementUri), Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uriMap.put(uri.toString(), Pair.create(true, entry.second));
        }
        askToRemoveExams();
    }

    /**
     * Disable buttons and make them opaque
     *
     * @param button target button
     * @param enable enable/ disable it
     */
    private void buttonSetEnabled(ImageButton button, boolean enable) {
        button.setEnabled(enable);
        button.setAlpha(enable ? 1.0f : 0.5f);
    }

    /**
     * Start exam editor on a press on the exam tab
     *
     * @param index tab which has been pressed
     */
    @Override
    public void onListFragmentPress(int index) {
        startSubApplication(Objects.requireNonNull(model.getLivedata().getValue()).get(index).getSortableKey(), ActionType.ACTION_LOCK);
    }

    /**
     * If a new exam has been created, show a dialog to set the name
     */
    private void showNameDialog() {
        AlertDialog.Builder builder;
        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.exam_name))
                .setView(input)
                .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                    String text = input.getText().toString();
                    if (model.getLivedata().contains(text))
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_exam_already_exists), Toast.LENGTH_SHORT).show();
                    else startSubApplication(text, ActionType.ACTION_EDITOR);

                })
                .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel())
                .show();
    }

    /**
     * Android callback if returning from a launched app
     *
     * @param requestCode code the intent was started with
     * @param resultCode  return from app
     * @param resultData  result data from app
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE_READ_STORAGE) {
            doNotRebuildUriMap = true;
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Called if user resumes from other app
     */
    @Override
    public void onResume() {
        if (doNotRebuildUriMap)
            doNotRebuildUriMap = false;
        else
            buildUriMap();
        super.onResume();
    }

    /**
     * Template to sub activity launch demultiplexer
     *
     * @param examName target exam
     * @param action   activity for open
     */
    protected abstract void startSubApplication(@Nullable String examName, ActionType action);

    /**
     * Tutorial using a showcase library
     */
    private void tutorial() {
        ShowcaseConfig config = new ShowcaseConfig();

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(add_button,
                getString(R.string.tutorial_addexam_button), getString(android.R.string.ok));

        sequence.addSequenceItem(del_button,
                getString(R.string.tutorial_delexam_button), getString(android.R.string.ok));

        sequence.addSequenceItem(edit_button,
                getString(R.string.tutorial_edt_button), getString(android.R.string.ok));

        sequence.addSequenceItem(findViewById(R.id.toolbar_overflow),
                getString(R.string.tutorial_menu_button), getString(android.R.string.ok));

        sequence.start();
    }

    /**
     * Different action types for the activity launcher
     */
    public enum ActionType {
        ACTION_EDITOR, ACTION_LOCK
    }

    /**
     * Shows a disclaimer at the launch of the activity to make sure, student and teach do test the
     * application properly
     *
     * @param message A message body for the disclaimer
     * @param onPositive is executed if the user hits the OK button
     */
    final protected void showDisclaimerDialog(@StringRes int message, DialogInterface.OnClickListener onPositive) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.disclaimer))
                .setMessage(message)
                .setPositiveButton(getString(android.R.string.ok), onPositive)
                .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                .setOnCancelListener(dialogInterface -> finish())
                .show();
    }
}
