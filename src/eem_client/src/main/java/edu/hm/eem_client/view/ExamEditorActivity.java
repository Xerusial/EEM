package edu.hm.eem_client.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import edu.hm.eem_client.R;
import edu.hm.eem_library.view.AbstractExamEditorActivity;

public class ExamEditorActivity extends AbstractExamEditorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_editor);
        svButton = findViewById(R.id.bt_save);
        delButton = findViewById(R.id.bt_del_doc);
        addButton = findViewById(R.id.bt_add_doc);
        addButton.setOnClickListener(v -> checkFileManagerPermissions());
        toolbar = findViewById(R.id.toolbar2);
    }

    @Override
    public void onClick(View v) {
        if(model.getLivedata().isEmpty()){
            Toast.makeText(getApplicationContext(), R.string.toast_select_documents_student, Toast.LENGTH_SHORT).show();
        }
        super.onClick(v);
    }
}
