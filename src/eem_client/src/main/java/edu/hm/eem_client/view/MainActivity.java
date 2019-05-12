package edu.hm.eem_client.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.HostViewModel;
import edu.hm.eem_client.net.ClientServiceManager;
import edu.hm.eem_library.view.ItemListFragment;

public class MainActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentPressListener, NsdManager.ResolveListener {
    public static final String PROF_FIELD = "Prof";
    public static final String ADDRESS_FIELD = "Address";
    public static final String PORT_FIELD = "Port";
    private ConnectivityManager cm;
    private ClientServiceManager clientServiceManager;
    private HostViewModel model;
    private Switch sw;
    private NsdManager nsdm;
    private ImageView progressBg;
    private ImageView progress;
    private AnimationDrawable progressAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cm = (ConnectivityManager) getApplicationContext().getSystemService( Context.CONNECTIVITY_SERVICE);
        sw = findViewById(R.id.sw_scan_services);
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> sw.setChecked(progress(scanNetwork(isChecked))));
        findViewById(R.id.bt_settings).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        nsdm = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        model = ViewModelProviders.of(this).get(HostViewModel.class);
        clientServiceManager = new ClientServiceManager(nsdm, model.getLivedata());
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
        NsdServiceInfo item = model.get(index);
        clientServiceManager.resolve(item, this);
    }

    @Override
    public void onListFragmentLongPress() {

    }

    @Override
    protected void onPause() {
        sw.setChecked(scanNetwork(false));
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        scanNetwork(false);
        super.onDestroy();
    }

    @Override
    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {

    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
        Intent intent = new Intent(this, LockedActivity.class);
        intent.putExtra(PROF_FIELD, serviceInfo.getServiceName());
        intent.putExtra(ADDRESS_FIELD, serviceInfo.getHost());
        intent.putExtra(PORT_FIELD, serviceInfo.getPort());
        startActivity(intent);
    }
}
