package edu.hm.eem_client.view;

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
import android.widget.Switch;
import android.widget.Toast;

import edu.hm.eem_client.R;
import edu.hm.eem_client.net.ClientProtocolManager;
import edu.hm.eem_library.model.StringMapViewModel;
import edu.hm.eem_library.view.ItemListFragment;

public class MainActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentPressListener {
    private StringMapViewModel<NsdServiceInfo> nsdServiceInfos;
    private NsdManager nsdm;
    private ConnectivityManager cm;
    private ClientProtocolManager clientProtocolManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nsdm = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        cm = (ConnectivityManager) getApplicationContext().getSystemService( Context.CONNECTIVITY_SERVICE);
        ((Switch)findViewById(R.id.sw_scan_services)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    buttonView.setChecked(scanNetwork());
            }
        });
        findViewById(R.id.bt_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        clientProtocolManager = new ClientProtocolManager(nsdm);
    }

    public boolean scanNetwork(boolean on){
        boolean ret = false;
        if (on) {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if ((activeNetwork != null) && (activeNetwork.isConnected())) {
                    ret = true;
                    clientProtocolManager.discover(on);
                } else
                    Toast.makeText(this, "Network is not up yet!", Toast.LENGTH_SHORT).show();
            }
        } else clientProtocolManager(on);
        return ret;
    }

    @Override
    public void onListFragmentPress(int index) {

    }

    @Override
    public void onListFragmentLongPress() {

    }
}
