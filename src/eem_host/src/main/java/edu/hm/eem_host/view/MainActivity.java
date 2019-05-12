package edu.hm.eem_host.view;

import android.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import edu.hm.eem_host.R;
import edu.hm.eem_library.model.ExamListViewModel;
import edu.hm.eem_library.view.ItemListFragment;

public class MainActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentPressListener {

    private ExamListViewModel model;
    private ImageButton del_button;
    private ImageButton edit_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = ViewModelProviders.of(this).get(ExamListViewModel.class);
        del_button = findViewById(R.id.bt_del_exam);
        del_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.getLivedata().removeSelected();
                updateButtonState();
            }
        });
        edit_button = findViewById(R.id.bt_edit_exam);
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = model.getLivedata().getSelected().sortableKey;
                startSubApplication(name, ExamEditorActivity.class);
            }
        });
        findViewById(R.id.bt_add_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNameDialog();
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
            }
        });
        findViewById(R.id.bt_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSubApplication(null, SettingsActivity.class);
            }
        });
        buttonSetEnabled(del_button, false);
        buttonSetEnabled(edit_button, false);
    }

    private void buttonSetEnabled(ImageButton button, boolean enable){
        button.setEnabled(enable);
        button.setAlpha(enable?1.0f:0.5f);
    }

    @Override
    public void onListFragmentPress(int index){
        startSubApplication(model.getLivedata().getValue().get(index).sortableKey, LockActivity.class);
    }

    @Override
    public void onListFragmentLongPress(){
        updateButtonState();
    }

    private void updateButtonState(){
        int sel_cnt = model.getLivedata().getSelectionCount();
        buttonSetEnabled(del_button,sel_cnt>0);
        buttonSetEnabled(edit_button,sel_cnt==1);
    }

    private void showNameDialog(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.exam_name));

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                if(model.getLivedata().contains(text))
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_exam_already_exists), Toast.LENGTH_SHORT).show();
                else startSubApplication(text, ExamEditorActivity.class);

            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void startSubApplication(@Nullable String examName, Class<?> cls){
        Intent intent = new Intent(MainActivity.this, cls);
        if(examName!=null) intent.putExtra("Name",examName);
        startActivity(intent);
    }
}
