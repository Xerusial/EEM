package edu.hm.eem_client.view;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

import edu.hm.eem_client.R;
import edu.hm.eem_client.net.ClientProtocolManager;
import edu.hm.eem_library.net.ProtocolHandler;
import edu.hm.eem_library.model.StudentExamDocumentItemViewModel;
import edu.hm.eem_library.model.TeacherExam;
import edu.hm.eem_library.view.AbstractMainActivity;

/** This activity is the heart of the lock mode. It features a do not disturb mode, exit monitoring,
 * a navhost and the lighthouse item.
 *
 */
public class LockedActivity extends AppCompatActivity implements DocumentExplorerFragment.OnDocumentsAcceptedListener {
    private ClientProtocolManager pm;
    private NotificationManager nm;
    private ImageView lightHouse;
    private StudentExamDocumentItemViewModel model;
    private ReaderFragment reader;
    private NavController navController;
    private String examName;
    private ImageView progressBg;
    private ImageView progress;
    private AnimationDrawable progressAnim;
    private boolean locked = false, drawerListenerOnline = true;
    private Pair<Boolean, Integer> currentNotificationFilter = Pair.create(false,0);

    /** Handler for syncing network signals to actions on the main UI thread.
     *
     */
    public class LockedHandler extends Handler implements ProtocolHandler {
        private LockedHandler(Looper looper) {
            super(looper);
        }

        /** Making the lighthouse symbol visible
         *
         * @param on turning it on or off
         */
        public void postLighthouse(boolean on) {
            this.post(() -> lightHouse.setVisibility(on ? View.VISIBLE : View.INVISIBLE));
        }

        /** Checking the proposed documents from the student if they are allowed.
         *  If the file is received too early, wait for all documents to be loaded using an observer.
         *
         * @param exam The received examfile from network
         */
        public void receiveExam(TeacherExam exam) {
            //has to be posted, because observe cannot be started on background thread
            this.post(()-> {
                if (!model.getLivedata().isEmpty()) {
                    if (model.checkExam(exam))
                        pm.allDocumentsAccepted();
                } else {
                    Observer obs = new Observer() {
                        @Override
                        public void onChanged(Object o) {
                            if (!model.getLivedata().isEmpty()) {
                                if (model.checkExam(exam))
                                    pm.allDocumentsAccepted();
                                model.getLivedata().removeObserver(this);
                            }
                        }
                    };
                    model.getLivedata().observe(LockedActivity.this, obs);

                }
            });
        }

        /** Start the asynctask for loading documents
         *
         */
        public void loadDocuments(){
            this.post(() -> {
                DocumentLoader loader = new DocumentLoader(LockedActivity.this, examName);
                loader.execute();
            });
        }

        /** Starting the lockmode and removing all not accepted documents in the exam from the listing.
         *
         */
        public void lock(){
            this.post(() -> {
                model.getLivedata().removeSelected();
                locked = true;
            });
        }

        /** Callback for bad packets to close the activity.
         *
         * @param hasMessage Indicator for having a message to display.
         * @param stringID Message for the reason of closing
         */
        public void gracefulShutdown(boolean hasMessage, @StringRes int stringID) {
            this.post(() -> {
                try {
                    if (hasMessage) {
                        Toast.makeText(LockedActivity.this, stringID, Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } catch (Exception e){
                    e.printStackTrace();
                }
            });
        }

        /** Showing a toast message
         *
         * @param resId Message to be displayed
         */
        @Override
        public void putToast(@StringRes int resId) {
            this.post(() -> Toast.makeText(LockedActivity.this.getApplicationContext(), resId, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);
        Intent intent = getIntent();
        InetAddress host = (InetAddress) intent.getSerializableExtra(ScanActivity.ADDRESS_FIELD);
        int port = intent.getIntExtra(ScanActivity.PORT_FIELD, 0);
        String profName = intent.getStringExtra(ScanActivity.PROF_FIELD);
        examName = intent.getStringExtra(AbstractMainActivity.EXAMNAME_FIELD);
        model = ViewModelProviders.of(this).get(StudentExamDocumentItemViewModel.class);
        navController = Navigation.findNavController(LockedActivity.this, R.id.nav_host);
        Bundle startArgs = new Bundle();
        startArgs.putString(AbstractMainActivity.EXAMNAME_FIELD, examName);
        startArgs.putString(ScanActivity.PROF_FIELD, profName);
        navController.setGraph(R.navigation.lockedactivity_nav, startArgs);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        FragmentManager navHostManager = Objects.requireNonNull(navHostFragment).getChildFragmentManager();
        FragmentManager.OnBackStackChangedListener listener = () -> {
                List<Fragment> frags = navHostManager.getFragments();
                if(!frags.isEmpty()) {
                    reader = (ReaderFragment) frags.get(0);
                }
        };
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if(destination.getId()==R.id.readerFragment){
                    navHostManager.addOnBackStackChangedListener(listener);
                } else {
                    navHostManager.removeOnBackStackChangedListener(listener);
                    reader = null;
                }
        });
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString(getString(R.string.preferences_username), "User@" + android.os.Build.MODEL);
        lightHouse = findViewById(R.id.lighthouse);
        progressBg = findViewById(R.id.progress_background);
        progress = findViewById(R.id.progress);
        progressAnim = (AnimationDrawable) progress.getDrawable();
        LockedHandler handler = new LockedHandler(Looper.getMainLooper());
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        pm = new ClientProtocolManager(this, host, port, name, handler);
    }

    /** Intercepting backpress if it was pressed by accident
     *
     */
    @Override
    public void onBackPressed() {
        if(locked && navController.getCurrentDestination().getId() == R.id.documentExplorerFragment) {
            drawerListenerOnline = false;
            showExitDialog();
        }else
            super.onBackPressed();
    }

    /** Dialog to be shown on intercepted back press.
     *
     */
    private void showExitDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_exit_student)
                .setPositiveButton(android.R.string.yes, (dialog, id) -> finish())
                .setNegativeButton(android.R.string.no, (dialog, id) -> dialog.cancel())
                .setOnCancelListener(dialog -> drawerListenerOnline = true);
        builder.show();
    }

    /** Making page turns possible using the volume rockers
     *
     * @param keyCode keycode of button
     * @param event keyevent (long press, etc)
     * @return if other key was pressed
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(reader!=null){
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                reader.turnPage(true);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                reader.turnPage(false);
                return true;
            }
        }
        return super.onKeyDown(keyCode,event);
    }

    /** Send a logoff signal and quit DnD
     *
     */
    @Override
    protected void onStop() {
        super.onStop();
        pm.quit();
        activateDnD(false);
    }

    /** When the user brings another activity to foreground, make sure this one quits and sends logoff
     *
     */
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    /** Documents accepted callback from the {@link DocumentExplorerFragment}
     *
     */
    @Override
    public void onDocumentsAccepted() {
        if(model.getLivedata().getSelectionCount()==0)
            pm.allDocumentsAccepted();
    }

    /** Asynctask for loading the documents, refreshing thumbnails and hashes.
     *
     */
    static class DocumentLoader extends AsyncTask<Void, Void, Void> {
        private final WeakReference<LockedActivity> context;
        private final String examName;

        public DocumentLoader(LockedActivity context, String examName) {
            this.context = new WeakReference<>(context);
            this.examName = examName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context.get().progress(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            context.get().model.openExam(examName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            context.get().progress(false);
            context.get().activateDnD(true);
        }
    }

    /** Method for activation and deactiviation of DnD mode
     *
     * @param on turn on or off
     */
    private void activateDnD(boolean on){
        if(on){
            currentNotificationFilter = Pair.create(true, nm.getCurrentInterruptionFilter());
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
        } else {
            if(currentNotificationFilter.first) {
                nm.setInterruptionFilter(currentNotificationFilter.second);
                currentNotificationFilter = Pair.create(false, 0);
            }
        }
    }

    /** Document loader animation
     *
     * @param on turn on
     */
    private void progress(boolean on){
        if(on){
            progressAnim.start();
        } else {
            progressAnim.stop();
        }
        progressBg.setVisibility(on?View.VISIBLE:View.GONE);
        progress.setVisibility(on?View.VISIBLE:View.GONE);
    }

    /** checking whether the notification drawer had been opened
     *
     * @param hasFocus if LockedActivity has the current focus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(locked && !hasFocus && drawerListenerOnline) {
            pm.notificationDrawerPulled();
        }
    }
}
