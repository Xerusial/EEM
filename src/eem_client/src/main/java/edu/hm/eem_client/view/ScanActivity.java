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
import android.widget.Toast;

import edu.hm.eem_client.R;
import edu.hm.eem_library.net.NsdService;
import edu.hm.eem_library.model.HostViewModel;
import edu.hm.eem_client.net.ClientServiceManager;
import edu.hm.eem_library.view.ItemListFragment;

public class ScanActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentPressListener, ClientServiceManager.ServiceReadyListener {
    public static final String PROF_FIELD = "Prof";
    public static final String ADDRESS_FIELD = "Address";
    public static final String PORT_FIELD = "Port";
    private ConnectivityManager cm;
    private ClientServiceManager clientServiceManager;
    private HostViewModel model;
    private Switch sw;
    private ImageView progressBg;
    private ImageView progress;
    private AnimationDrawable progressAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        cm = (ConnectivityManager) getApplicationContext().getSystemService( Context.CONNECTIVITY_SERVICE);
        sw = findViewById(R.id.sw_scan_services);
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> sw.setChecked(progress(scanNetwork(isChecked))));

        model = ViewModelProviders.of(this).get(HostViewModel.class);
        clientServiceManager = new ClientServiceManager(getApplicationContext(), model.getLivedata(), this);
        progressBg = findViewById(R.id.progress_background);
        progress = findViewById(R.id.progress);
        progressAnim = (AnimationDrawable) progress.getDrawable();
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
        clientServiceManager.discover(on);
        return ret;
    }

    private boolean progress(boolean on){
        if(on){
            progressBg.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            progressAnim.start();
        } else {
            progressBg.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            progressAnim.stop();
        }
        return on;
    }

    @Override
    public void onListFragmentPress(int index) {
        NsdService item = model.get(index);
        clientServiceManager.resolve(item);
    }

    @Override
    protected void onPause() {
        sw.setChecked(scanNetwork(false));
        model.getLivedata().clean(false);
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
        startActivity(intent);
    }
}
