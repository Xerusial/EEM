package edu.hm.eem_host.view;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import edu.hm.eem_host.R;
import edu.hm.eem_host.model.ExamDocument;
import edu.hm.eem_host.model.ExamHost;
import edu.hm.eem_host.model.SelectableItem;

public class ExamEditorActivity extends AppCompatActivity implements View.OnClickListener, SelectableItemListFragment.OnListFragmentPressListener{

    private ExamHost model;

    private EditText pwFields[] = new EditText[3];
    private CheckBox allDocAllowedField;
    private ImageButton del_button;
    private boolean examIsNew;

    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private final int READ_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(ExamHost.class);
        examIsNew = model.setCurrent(getIntent().getStringExtra("Name"));
        setContentView(R.layout.activity_exam_editor);
        pwFields[0] = findViewById(R.id.oldPass);
        pwFields[1] = findViewById(R.id.pass);
        pwFields[2] = findViewById(R.id.repPass);
        allDocAllowedField = findViewById(R.id.allDocsAllowed);
        del_button = findViewById(R.id.bt_del_doc);
        findViewById(R.id.bt_add_doc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFileManagerPermissions();
            }
        });
        del_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.getLivedata().removeSelected();
            }
        });
        findViewById(R.id.bt_save).setOnClickListener(this);
        ((CheckBox)findViewById(R.id.showPass)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    for(EditText t : pwFields)
                        if(isChecked) t.setTransformationMethod(null);
                        else t.setTransformationMethod(new PasswordTransformationMethod());
            }
        });
        ((TextView)findViewById(R.id.examName)).setText(model.getLivedata().getCurrent().getName());
        allDocAllowedField.setChecked(model.getLivedata().getCurrent().allDocumentsAllowed);
    }

    private void setFields(){
        model.getLivedata().getCurrent().allDocumentsAllowed = allDocAllowedField.isChecked();
    }

    @Override
    public void onClick(View v) {
        String pw = pwFields[1].getText().toString();
        if(!pw.equals(pwFields[2].getText().toString())){
            Toast.makeText(getApplicationContext(),getString(R.string.toast_passwords_do_not_match),Toast.LENGTH_SHORT).show();
            return;
        }
        if(!examIsNew){
            if (!model.getLivedata().getCurrent().checkPW(pwFields[0].getText().toString())) {
                Toast.makeText(getApplicationContext(),getString(R.string.toast_old_password_incorrect), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        model.getLivedata().getCurrent().setPassword(pw);
        setFields();
        model.writeExamToFile();
        finish();
    }

    @Override
    public void onListFragmentPress(int index) {

    }

    @Override
    public void onListFragmentLongPress() {
        int sel_cnt = 0;
        for(SelectableItem<ExamDocument> item : model.getLivedata().getValue())
            if(item.selected) sel_cnt++;
        del_button.setEnabled(sel_cnt>0);
    }

    private void openFileManager(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void checkFileManagerPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
                Toast.makeText(getApplicationContext(),getString(R.string.toast_read_files_warning), Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            openFileManager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openFileManager();
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                String fileName = (new File(uri.getPath())).getName();
                model.getLivedata().add(new ExamDocument(fileName,uri.getPath()));
            }
        }
    }



}
