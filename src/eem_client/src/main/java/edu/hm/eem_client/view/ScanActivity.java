package edu.hm.eem_client.view;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.github.druk.dnssd.DNSSDService;

import edu.hm.eem_client.R;
import edu.hm.eem_library.net.NsdService;
import edu.hm.eem_library.model.HostItemViewModel;
import edu.hm.eem_client.net.ClientServiceManager;
import edu.hm.eem_library.view.AbstractMainActivity;
import edu.hm.eem_library.view.ItemListFragment;

public class ScanActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentPressListener, ClientServiceManager.ServiceReadyListener {
    public static final String PROF_FIELD = "Prof";
    public static final String ADDRESS_FIELD = "Address";
    public static final String PORT_FIELD = "Port";
    private ConnectivityManager cm;
    private ClientServiceManager clientServiceManager;
    private HostItemViewModel model;
    private Switch sw;
    private ImageView progressBg;
    private ImageView progress;
    private AnimationDrawable progressAnim;
    private TextView uiLockView;
    private boolean uiLocked = false;
    private String examName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        cm = (ConnectivityManager) getApplicationContext().getSystemService( Context.CONNECTIVITY_SERVICE);
        sw = findViewById(R.id.sw_scan_services);
        sw.setOnClickListener(v -> sw.setChecked(progress(scanNetwork(sw.isChecked()))));
        model = ViewModelProviders.of(this).get(HostItemViewModel.class);
        clientServiceManager = new ClientServiceManager(getApplicationContext(), model.getLivedata(), this);
        progressBg = findViewById(R.id.progress_background);
        progress = findViewById(R.id.progress);
        uiLockView = findViewById(R.id.ui_locker);
        progressAnim = (AnimationDrawable) progress.getDrawable();
        examName = getIntent().getStringExtra(AbstractMainActivity.EXAMNAME_FIELD);
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(examName);
    }

    private boolean scanNetwork(boolean on){
        boolean ret = false;
        if (on && cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if ((activeNetwork != null) && (activeNetwork.isConnected())) {
                ret = true;
            } else {
                Toast.makeText(this, "Network is not up yet!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if(on) model.getLivedata().clean(true);
        clientServiceManager.discover(on);
        return ret;
    }

    private boolean progress(boolean on){
        if(on){
            progressAnim.start();
        } else {
            progressAnim.stop();
        }
        progressBg.setVisibility(on?View.VISIBLE:View.GONE);
        progress.setVisibility(on?View.VISIBLE:View.GONE);
        return on;
    }

    @Override
    public void onListFragmentPress(int index) {
        if(!uiLocked) {
            lock(true);
            NsdService item = model.get(index);
            clientServiceManager.resolve(item);
        }
    }

    private void lock(boolean enable){
        uiLocked = enable;
        uiLockView.setVisibility(enable?View.VISIBLE:View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        sw.setChecked(progress(scanNetwork(false)));
        model.getLivedata().clean(false);
        lock(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        scanNetwork(false);
        super.onDestroy();
    }

    @Override
    public void onServiceReady(NsdService nsdService) {
        Intent intent = new Intent(this, LockedActivity.class);
        intent.putExtra(PROF_FIELD, nsdService.serviceName);
        intent.putExtra(ADDRESS_FIELD, nsdService.address);
        intent.putExtra(PORT_FIELD, nsdService.port);
        intent.putExtra(AbstractMainActivity.EXAMNAME_FIELD, examName);
        startActivity(intent);
    }

    @Override
    public void operationFailed(DNSSDService service, int errorCode) {
        lock(false);
    }
}
