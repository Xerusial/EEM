package edu.hm.eem_client.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import edu.hm.eem_client.R;
import edu.hm.eem_library.model.HostViewModel;
import edu.hm.eem_client.net.ClientServiceManager;
import edu.hm.eem_library.view.ItemListFragment;

public class MainActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentPressListener {
    private ConnectivityManager cm;
    private ClientServiceManager clientServiceManager;
    private HostViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NsdManager nsdm = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        cm = (ConnectivityManager) getApplicationContext().getSystemService( Context.CONNECTIVITY_SERVICE);
        ((Switch)findViewById(R.id.sw_scan_services)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(scanNetwork(isChecked));
            }
        });
        findViewById(R.id.bt_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        ProgressBar pb = findViewById(R.id.progressBar);
        model = ViewModelProviders.of(this).get(HostViewModel.class);
        clientServiceManager = new ClientServiceManager(this, nsdm, pb, model.getLivedata());
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

    @Override
    public void onListFragmentPress(int index) {
        scanNetwork(false);
        NsdServiceInfo item = model.get(index);
        clientServiceManager.resolve(item);
    }

    @Override
    public void onListFragmentLongPress() {

    }
}
