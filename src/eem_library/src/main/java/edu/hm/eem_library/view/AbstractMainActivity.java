package edu.hm.eem_library.view;

import android.app.AlertDialog;

import androidx.lifecycle.ViewModelProviders;

import android.content.ContentResolver;
import android.content.Context;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Pair;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import edu.hm.eem_library.model.ExamListViewModel;
import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.model.StudentExam;
import edu.hm.eem_library.model.ThumbnailedExamDocument;

public abstract class AbstractMainActivity extends DocumentPickerActivity implements ItemListFragment.OnListFragmentPressListener {

    public static final String EXAMNAME_FIELD = "ExamName";

    protected ExamFactory.ExamType examType;

    public enum ActionType {
        ACTION_EDITOR, ACTION_LOCK
    }

    private ExamListViewModel model;
    private ImageButton del_button;
    private ImageButton edit_button;
    private TreeMap<String, Pair<Boolean, List<String>>> uriMap;
    private String replacementUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abstract_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.burger_popup);
        setActionBar(toolbar);
        model = ViewModelProviders.of(this).get(ExamListViewModel.class);
        del_button = findViewById(R.id.bt_del_exam);
        del_button.setOnClickListener(v -> removeSelected());
        edit_button = findViewById(R.id.bt_edit_exam);
        edit_button.setOnClickListener(v -> {
            String name = model.getLivedata().getSelected().getName();
            startSubApplication(name, ActionType.ACTION_EDITOR);
        });
        findViewById(R.id.bt_add_exam).setOnClickListener(v -> {
            showNameDialog();
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
        });
        model.getLivedata().observe(this, sortableItems -> {
            int sel_cnt = model.getLivedata().getSelectionCount();
            buttonSetEnabled(del_button,sel_cnt>0);
            buttonSetEnabled(edit_button,sel_cnt==1);
        });
    }



    private void buildUriMap(){
        ExamFactory factory = new ExamFactory(examType);
        ContentResolver resolver = getContentResolver();
        uriMap = new TreeMap<>();
        for(SelectableSortableItem<File> container : model.getLivedata().getValue()){
            try {
                FileInputStream fis = new FileInputStream(container.item);
                StudentExam exam = factory.extract(fis);
                fis.close();
                for(ExamDocument doc : exam.getAllowedDocuments()){
                    String uriString = doc.getUriString();
                    if(uriString != null) {
                        if(!uriMap.containsKey(uriString)) {
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
                        entry.second.add(container.item.getName());
                    }
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        model.getLivedata().clearSelection();
        askToRemoveExams();
    }

    private void removeSelected(){
        if(model.getLivedata().getSelectionCount()!=0) {
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

    private void askToRemoveExams(){
        boolean entryFound = false;
        for(Map.Entry<String, Pair<Boolean, List<String>>> entry : uriMap.entrySet()) {
            if(!entry.getValue().first){
                // File was deleted: get new File
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(this);
                TextView textView = new TextView(this);
                Iterator<String> it = entry.getValue().second.iterator();
                StringBuilder sb = new StringBuilder(it.next());
                for(;it.hasNext();){
                    sb.append(", ");
                    sb.append(it.next());
                }
                textView.setText(getString(entry.getValue().second.size()>1?
                        R.string.dialog_document_not_found_pl:
                        R.string.dialog_document_not_found_sing,
                        getNameFromUri(Uri.parse(entry.getKey())), sb.toString()));
                builder.setCustomTitle(textView);
                builder.setPositiveButton(getString(R.string.dialog_document_not_found_bt_pos), (dialog, which) -> {
                    replacementUri = entry.getKey();
                    dialog.dismiss();
                    checkFileManagerPermissions();
                });
                builder.setNeutralButton(getString(R.string.dialog_document_not_found_bt_neutral), (dialog, which) -> {
                    getContentResolver().releasePersistableUriPermission(Uri.parse(entry.getKey()),
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    uriMap.remove(entry.getKey());
                    for(String s : entry.getValue().second)
                        model.getLivedata().setSelected(s, false);
                    dialog.dismiss();
                    askToRemoveExams();
                });
                builder.setNegativeButton(getString(R.string.dialog_document_not_found_bt_neg), (dialog, which) -> {
                    removeSelected();
                    dialog.cancel();
                    finish();
                });
                builder.show();
                entryFound = true;
                break;
            }
        }
        if(!entryFound) removeSelected();
    }

    @Override
    void handleDocument(@Nullable Uri uri) {
        if(uri!=null) {
            Pair<Boolean, List<String>> entry = uriMap.get(replacementUri);
            ExamDocument doc = ThumbnailedExamDocument.getInstance(this, uri).item;
            for (String s : entry.second) {
                ExamFactory factory = new ExamFactory(examType);
                for (SelectableSortableItem<File> container : model.getLivedata().getValue()) {
                    if(container.item.getName().equals(s)) {
                        try {
                            FileInputStream fis = new FileInputStream(container.item);
                            StudentExam exam = factory.extract(fis);
                            fis.close();
                            List<ExamDocument> list = exam.getAllowedDocuments();
                            for (int i = 0; i < list.size(); i++) {
                                ExamDocument oldDoc = list.get(i);
                                if (oldDoc.getUriString().equals(replacementUri)) {
                                    ExamDocument newDoc = (ExamDocument) doc.clone();
                                    if(oldDoc.getHash()==null)
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

    private void buttonSetEnabled(ImageButton button, boolean enable){
        button.setEnabled(enable);
        button.setAlpha(enable?1.0f:0.5f);
    }

    @Override
    public void onListFragmentPress(int index){
        startSubApplication(model.getLivedata().getValue().get(index).getSortableKey(), ActionType.ACTION_LOCK);
    }

    private void showNameDialog(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.exam_name));

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
            String text = input.getText().toString();
            if(model.getLivedata().contains(text))
                Toast.makeText(getApplicationContext(), getString(R.string.toast_exam_already_exists), Toast.LENGTH_SHORT).show();
            else startSubApplication(text, ActionType.ACTION_EDITOR);

        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onResume(){
        buildUriMap();
        super.onResume();
    }

    protected abstract void startSubApplication(@Nullable String examName, ActionType action);
}
