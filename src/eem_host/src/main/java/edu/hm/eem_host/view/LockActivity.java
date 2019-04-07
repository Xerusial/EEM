package edu.hm.eem_host.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import edu.hm.eem_host.R;
import edu.hm.eem_library.WIFIANDLOCATIONCHECKER;
import edu.hm.eem_library.net.HotspotManager;

public class LockActivity extends AppCompatActivity implements WIFIANDLOCATIONCHECKER.onWifiAndLocationEnabledListener, HotspotManager.OnHotspotEnabledListener{
    private final IntentFilter intentFilter = new IntentFilter();
    private HotspotManager hotspotManager;
    private WifiManager wm;
    private LocationManager lm;
    private boolean netRequirementsGathered;
    private TextView netName;
    private TextView netPw;
    private TextView conDevices;
    private Switch swStartHotspot;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        netRequirementsGathered = false;
        netName = findViewById(R.id.net_name);
        netPw = findViewById(R.id.net_pw);
        conDevices = findViewById(R.id.con_devices);
        swStartHotspot = findViewById(R.id.sw_start_hotspot);
        swStartHotspot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
        lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        WIFIANDLOCATIONCHECKER.check(this, wm, lm);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
                    Log.d("EEM/LockActivity", "Network state change triggered!");
                } else if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)){
                    Log.d("EEM/LockActivity", "Wifi state change triggered!");
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
