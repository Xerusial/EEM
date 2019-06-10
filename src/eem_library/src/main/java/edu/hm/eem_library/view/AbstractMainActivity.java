package edu.hm.eem_library.view;

import android.app.AlertDialog;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ExamListViewModel;

public abstract class AbstractMainActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentPressListener {

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
        setActionBar(toolbar);
        model = ViewModelProviders.of(this).get(ExamListViewModel.class);
        del_button = findViewById(R.id.bt_del_exam);
        del_button.setOnClickListener(v -> model.getLivedata().removeSelected());
        edit_button = findViewById(R.id.bt_edit_exam);
        edit_button.setOnClickListener(v -> {
            String name = model.getLivedata().getSelected().sortableKey;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.burger_popup, menu);
        return true;
    }

    private void buttonSetEnabled(ImageButton button, boolean enable){
        button.setEnabled(enable);
        button.setAlpha(enable?1.0f:0.5f);
    }

    @Override
    public void onListFragmentPress(int index){
        startSubApplication(model.getLivedata().getValue().get(index).sortableKey, ActionType.ACTION_LOCK);
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
