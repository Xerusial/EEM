package edu.hm.eem_client.view;

import android.app.AlertDialog;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

import edu.hm.eem_client.R;
import edu.hm.eem_client.net.ClientProtocolManager;
import edu.hm.eem_library.net.ProtocolHandler;
import edu.hm.eem_library.model.StudentExamDocumentItemViewModel;
import edu.hm.eem_library.model.TeacherExam;
import edu.hm.eem_library.view.AbstractMainActivity;

public class LockedActivity extends AppCompatActivity implements DocumentExplorerFragment.OnDocumentsAcceptedListener {
    private ClientProtocolManager pm;
    private ImageView lightHouse;
    private StudentExamDocumentItemViewModel model;
    private ReaderFragment reader;
    private NavController navController;

    public class LockedHandler extends Handler implements ProtocolHandler {
        private LockedHandler(Looper looper) {
            super(looper);
        }

        public void postLighthouse(boolean on) {
            this.post(() -> lightHouse.setVisibility(on ? View.VISIBLE : View.INVISIBLE));
        }

        public boolean receiveExam(TeacherExam exam) {
            return model.checkExam(exam);
        }

        public void gracefulShutdown(@Nullable String message) {
            this.post(() -> {
                if (message != null) {
                    Log.e(LockedActivity.this.getPackageName(), message);
                }
                finish();
            });
        }

        @Override
        public void putToast(int resId) {
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
        String examName = intent.getStringExtra(AbstractMainActivity.EXAMNAME_FIELD);
        model = ViewModelProviders.of(this).get(StudentExamDocumentItemViewModel.class);
        model.openExam(examName);
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
        LockedHandler handler = new LockedHandler(Looper.getMainLooper());
        pm = new ClientProtocolManager(this, host, port, name, handler);
    }

    @Override
    public void onBackPressed() {
        if(navController.getCurrentDestination().getId() == R.id.documentExplorerFragment)
            showExitDialog();
        else
            super.onBackPressed();
    }

    private void showExitDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_exit_student)
                .setPositiveButton(android.R.string.yes, (dialog, id) -> finish())
                .setNegativeButton(android.R.string.no, (dialog, id) -> dialog.cancel());
        builder.show();
    }

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

    @Override
    protected void onStop() {
        super.onStop();
        pm.quit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onDocumentsAccepted() {
        pm.allDocumentsAccepted();
    }
}
