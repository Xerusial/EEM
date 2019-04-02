package edu.hm.eem_host.view;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import edu.hm.eem_host.R;
import edu.hm.eem_host.model.ExamListHost;
import edu.hm.eem_host.model.Nameable;
import edu.hm.eem_host.model.SelectableItem;

public class MainActivity extends AppCompatActivity implements SelectableItemListFragment.OnListFragmentPressListener {

    private enum ActivityType {EDITOR,LOCK}
    private ExamListHost model;
    private ImageButton del_button;
    private ImageButton edit_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(ExamListHost.class);
        setContentView(R.layout.activity_main);
        del_button = findViewById(R.id.bt_del_exam);
        del_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.getLivedata().removeSelected();
            }
        });
        edit_button = findViewById(R.id.bt_edit_exam);
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = null;
                for(SelectableItem<Nameable> exam : model.getLivedata().getValue()){
                    if(exam.selected) {
                        name = exam.dataItem.getName();
                        break;
                    }
                }
                if(name!=null)
                    startSubApplication(name, ActivityType.EDITOR);
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
        buttonSetEnabled(del_button, false);
        buttonSetEnabled(edit_button, false);
    }

    private void buttonSetEnabled(ImageButton button, boolean enable){
        button.setEnabled(enable);
        button.setAlpha(enable?1.0f:0.5f);
    }

    @Override
    public void onListFragmentPress(int index){
        startSubApplication(model.getLivedata().getValue().get(index).dataItem.getName(), ActivityType.LOCK);
    }

    @Override
    public void onListFragmentLongPress(){
        int sel_cnt = 0;
        for(SelectableItem<Nameable> item : model.getLivedata().getValue())
            if(item.selected) sel_cnt++;
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

        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                boolean namesChecked = true;
                for(SelectableItem item : model.getLivedata().getValue())
                    if(item.dataItem.getName().equals(text)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_exam_already_exists), Toast.LENGTH_SHORT).show();
                        namesChecked= false;
                        break;
                    }
                if(namesChecked) startSubApplication(text, ActivityType.EDITOR);

            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void startSubApplication(String examName, ActivityType type){
        Intent intent;
        if(type==ActivityType.EDITOR)
            intent = new Intent(MainActivity.this, ExamEditorActivity.class);
        else
            intent = new Intent(MainActivity.this, LockActivity.class);
        intent.putExtra("Name",examName);
        startActivity(intent);
    }
}
