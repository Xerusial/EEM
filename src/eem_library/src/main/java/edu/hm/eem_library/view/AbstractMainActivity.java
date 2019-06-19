package edu.hm.eem_library.view;

import android.app.AlertDialog;

import androidx.lifecycle.ViewModelProviders;

import android.content.ContentResolver;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ExamDocument;
import edu.hm.eem_library.model.ExamFactory;
import edu.hm.eem_library.model.ExamListViewModel;
import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.model.StudentExam;

public abstract class AbstractMainActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentPressListener {

    public static final String EXAMNAME_FIELD = "ExamName";

    public enum ActionType {
        ACTION_EDITOR, ACTION_LOCK
    }

    private ExamListViewModel model;
    private ImageButton del_button;
    private ImageButton edit_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abstract_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.burger_popup);
        setActionBar(toolbar);
        model = ViewModelProviders.of(this).get(ExamListViewModel.class);
        del_button = findViewById(R.id.bt_del_exam);
        del_button.setOnClickListener(v -> model.getLivedata().removeSelected());
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
        //Check doc existence
        ExamFactory factory = new ExamFactory(ExamFactory.ExamType.STUDENT);
        ContentResolver resolver = getContentResolver();
        List<UriPermission> permList = resolver.getPersistedUriPermissions()
        Map<Uri, Boolean> containsMap = new HashMap<>(permList.size());
        for(UriPermission perm : permList){
            containsMap.put(UriPermission.)
        }
        for(SelectableSortableItem<File> container : model.getLivedata().getValue()){
            try {
                FileInputStream fis = new FileInputStream(container.item);
                StudentExam exam = factory.extract(fis);
                fis.close();
                for(ExamDocument doc : exam.getAllowedDocuments()){
                    Uri.parse(doc.getUri()).
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
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

    protected abstract void startSubApplication(@Nullable String examName, ActionType action);
}
