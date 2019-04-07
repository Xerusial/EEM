package edu.hm.eem_client;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import edu.hm.eem_library.WIFIANDLOCATIONCHECKER;

public class MainActivity extends AppCompatActivity implements WIFIANDLOCATIONCHECKER.onWifiAndLocationEnabledListener {
    private WifiManager wm;
    private LocationManager lm;
    private BroadcastReceiver wifiScanReceiver;
    private IntentFilter intentFilter;
    private int networkID;
    private boolean networkUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        networkUp = false;
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lm = (LocationManager) getApplicationContext().getSystemService( Context.LOCATION_SERVICE );
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {

                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    if (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false))
                        processScanResults();
                } else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if(networkInfo.isConnected()) {
                        // Wifi is connected
                        Log.d("EEM_Client", "Wifi is connected: " + String.valueOf(networkInfo));
                    }
                } else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && ! networkInfo.isConnected()) {
                        // Wifi is disconnected
                        Log.d("EEM_Client", "Wifi is disconnected: " + String.valueOf(networkInfo));
                    }
                }
            }
        };

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WIFIANDLOCATIONCHECKER.check( MainActivity.this , wm, lm);
            }
        });
    }

    @Override
    public void onWifiAndLocationEnabled() {
        if (wm.startScan())
            getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        else
            processScanResults(); //for Android Q, startScan wont work
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == WIFIANDLOCATIONCHECKER.WIFI_OR_LOCATION_REQUEST) {
            WIFIANDLOCATIONCHECKER.check(this, wm, lm);
        }
    }

    private void processScanResults(){
        getApplicationContext().unregisterReceiver(wifiScanReceiver);
        String SSID = null;
        List<ScanResult> list = wm.getScanResults();
        for(ScanResult w : list){
            if(w.SSID.matches("Android.*")) {
                SSID = w.SSID;
                break;
            }
        }
        if(SSID != null) showPasswordDialog(SSID);
    }

    private void showPasswordDialog(final String SSID){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.wifi_password) + "" + SSID);

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                openNetwork(SSID, text);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openNetwork(final String SSID, final String key){
        boolean continueSetup = true;
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + SSID + "\"";
        config.preSharedKey = "\"" + key + "\"";
        for(int i = 0; i<5; i++){
            if(continueSetup) {
                switch (i) {
                    case 0:
                        continueSetup = ((networkID = wm.addNetwork(config)) != -1);
                    case 1:
                        continueSetup = wm.disconnect();
                    case 2:
                        continueSetup = wm.enableNetwork(networkID, true);
                    case 3:
                        continueSetup = wm.reconnect();
                        networkUp = true;
                }
            } else {
                Toast.makeText(this, R.string.toast_wifi_connection_failed, Toast.LENGTH_SHORT);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == WIFIANDLOCATIONCHECKER.PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION){
            if(grantResults.length > 0
                    && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                WIFIANDLOCATIONCHECKER.check(this, wm, lm);
            else
                finish();
        }
    }

    @Override
    protected void onStop() {
        if(networkUp) {
            wm.disconnect();
            wm.removeNetwork(networkID);
            wm.reconnect();
        }
        super.onStop();
    }
}
