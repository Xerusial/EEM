package edu.hm.eem_client.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.ExamFactory;
import edu.hm.eem_library.view.AbstractMainActivity;
import edu.hm.eem_library.view.AboutActivity;

public class MainActivity extends AbstractMainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        examType = ExamFactory.ExamType.STUDENT;
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = false;
        Intent intent = null;
        switch(item.getItemId()){
            case R.id.menu_settings:
                intent = new Intent(this, SettingsActivity.class);
                ret = true;
                break;
            case R.id.menu_help:
                ret = true;
                break;
            case R.id.menu_about:
                intent = new Intent(this, AboutActivity.class);
                ret = true;
                break;
        }
        if(intent!=null) startActivity(intent);
        return ret;
    }

    @Override
    protected void startSubApplication(@Nullable String examName, ActionType action) {
        Intent intent = null;
        switch (action){
            case ACTION_EDITOR:
                intent = new Intent(this, ExamEditorActivity.class);
                break;
            case ACTION_LOCK:
                intent = new Intent(this, ScanActivity.class);
                break;
        }
        if (examName != null){
            intent.putExtra(EXAMNAME_FIELD, examName);
            startActivity(intent);
        }
    }
}
