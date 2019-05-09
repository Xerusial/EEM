package edu.hm.eem_host.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.net.ServerSocket;

import edu.hm.eem_host.R;
import edu.hm.eem_library.model.DeviceViewModel;
import edu.hm.eem_host.net.HostProtocolManager;
import edu.hm.eem_host.net.HostServiceManager;
import edu.hm.eem_library.net.WIFIANDLOCATIONCHECKER;
import edu.hm.eem_library.net.HotspotManager;

public class LockActivity extends AppCompatActivity
        implements WIFIANDLOCATIONCHECKER.onWifiAndLocationEnabledListener,
        HotspotManager.OnHotspotEnabledListener{
    private final IntentFilter intentFilter = new IntentFilter();
    private HotspotManager hotspotManager;
    private WifiManager wm;
    private ConnectivityManager cm;
    private NsdManager nsdm;
    private boolean netRequirementsGathered;
    private TextView netName;
    private TextView netPw;
    private Switch swStartHotspot;
    private BroadcastReceiver broadcastReceiver;
    private HostProtocolManager hostProtocolManager;
    private HostServiceManager hostServiceManager;
    private DeviceViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        netRequirementsGathered = false;
        netName = findViewById(R.id.net_name);
        netPw = findViewById(R.id.net_pw);
        swStartHotspot = findViewById(R.id.sw_start_hotspot);
        swStartHotspot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO Resolve not activatable if location services are not up yet
                if(netRequirementsGathered){
                    if(isChecked)
                        hotspotManager.turnOnHotspot();
                    else
                        hotspotManager.turnOffHotspot();
                } else
                    swStartHotspot.setChecked(false);
            }
        });
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        nsdm = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        model = ViewModelProviders.of(this).get(DeviceViewModel.class);
        WIFIANDLOCATIONCHECKER.check(this, wm, lm);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getAction();
                switch(s){
                    case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        if (networkInfo.isConnected()) {
                            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                            String profName = sharedPref.getString(getString(R.string.preferences_username), "Examprof");
                            try {
                                ServerSocket serverSocket = new ServerSocket(0);
                                hostServiceManager = new HostServiceManager(serverSocket, profName, nsdm);
                                hostProtocolManager = new HostProtocolManager(LockActivity.this, serverSocket, model.getLivedata());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // Wifi is connected
                            Log.d("EEM_Host", "Wifi is connected: " + networkInfo);
                        }
                        break;
                    case ConnectivityManager.CONNECTIVITY_ACTION:
                        networkInfo = cm.getActiveNetworkInfo();
                        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && !networkInfo.isConnected()) {
                            // Wifi is disconnected
                            Log.d("EEM_Host", "Wifi is disconnected: " + networkInfo);
                        }
                        break;
                }
            }
        };
    }

    @Override
    public void onWifiAndLocationEnabled() {
        hotspotManager = new HotspotManager(wm, this);
        netRequirementsGathered = true;
    }

    @Override
    public void OnHotspotEnabled(boolean enabled, @Nullable WifiConfiguration wifiConfiguration) {
        if(enabled) {
            netName.setText(wifiConfiguration.SSID);
            netPw.setText(wifiConfiguration.preSharedKey);
            registerReceiver(broadcastReceiver,intentFilter);
        } else {
            netName.setText(getString(R.string.blank));
            netPw.setText(getString(R.string.blank));
            unregisterReceiver(broadcastReceiver);
        }
    }
}
