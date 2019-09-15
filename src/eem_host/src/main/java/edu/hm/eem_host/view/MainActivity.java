package edu.hm.eem_host.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import edu.hm.eem_host.R;
import edu.hm.eem_library.model.ExamFactory;
import edu.hm.eem_library.view.AboutActivity;
import edu.hm.eem_library.view.AbstractMainActivity;

/**
 * Host side {@link AbstractMainActivity}
 */
public class MainActivity extends AbstractMainActivity {

    /**
     * Create the fitting exam factory
     *
     * @param savedInstanceState Android basics
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        examType = ExamFactory.ExamType.TEACHER;
        super.onCreate(savedInstanceState);
        showDisclaimerDialog(R.string.disclaimer_text_teacher, (dialogInterface, i) -> dialogInterface.dismiss());
    }

    /**
     * Callbacks for options menu (Burger)
     *
     * @param item item that has been clicked on
     * @return if callback for menu item has been found
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = false;
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.menu_settings:
                intent = new Intent(this, SettingsActivity.class);
                ret = true;
                break;
            case R.id.menu_about:
                intent = new Intent(this, AboutActivity.class);
                ret = true;
                break;
        }
        if (intent != null) startActivity(intent);
        return ret;
    }

    /**
     * Demultiplexer for all intents started from the {@link AbstractMainActivity}
     *
     * @param examName The target exam for the launched activities
     * @param action action type to launch
     */
    @Override
    protected void startSubApplication(@Nullable String examName, ActionType action) {
        Intent intent = null;
        switch (action) {
            case ACTION_EDITOR:
                intent = new Intent(this, ExamEditorActivity.class);
                break;
            case ACTION_LOCK:
                intent = new Intent(this, LockActivity.class);
                break;
        }
        if (examName != null) {
            intent.putExtra(EXAMNAME_FIELD, examName);
            startActivity(intent);
        }
    }
}
