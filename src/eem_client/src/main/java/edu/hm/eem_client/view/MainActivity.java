package edu.hm.eem_client.view;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.ExamFactory;
import edu.hm.eem_library.view.AbstractMainActivity;
import edu.hm.eem_library.view.AboutActivity;

/** The first activity of this application, only used to correctly branch into other activities.
 * Main functionality is in the {@link AbstractMainActivity} in the eem_library.
 *
 */
public class MainActivity extends AbstractMainActivity {
    private static final int REQUEST_ACCESS_DND = 2;
    private NotificationManager nm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        examType = ExamFactory.ExamType.STUDENT;
        super.onCreate(savedInstanceState);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        checkDnDPermission(true);
    }

    /** Branch into activities launched from the burger menu
     *
     * @param item which has been touched
     * @return launchintent for item was found
     */
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

    /** Starts follow-up activities from various input touchpoints in this activity.
     *
     * @param examName currently focused exam, may be null
     * @param action specifies the next activity to be launched
     */
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

    /** Do not disturb mode/ zen mode needs to be enabled in this app, in order to hide notifications
     * form the student. This needs a special permission, which is checked by this function.
     *
     * @param firstTry first time to ask for permission or has the user already cancelled the req?
     */
    private void checkDnDPermission(boolean firstTry){
        if(!nm.isNotificationPolicyAccessGranted()){
            if(firstTry) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.dialog_dnd_required)
                        .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                             Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                             startActivityForResult(intent, REQUEST_ACCESS_DND);
                        })
                        .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel())
                        .setOnCancelListener(dialog -> finish())
                        .show();

            } else
                finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_ACCESS_DND){
            checkDnDPermission(false);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
