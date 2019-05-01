package edu.hm.eem_host.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.lang.reflect.Method;

import edu.hm.eem_library.net.WIFIANDLOCATIONCHECKER;

public class WifiDirectActivity extends AppCompatActivity implements WIFIANDLOCATIONCHECKER.onWifiAndLocationEnabledListener {
    private final String name = "Exammode";
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiManager wm;
    private LocationManager lm;
    private WifiP2pManager.GroupInfoListener groupInfoListener;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager manager;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lm = (LocationManager) getApplicationContext().getSystemService( Context.LOCATION_SERVICE );

        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        WIFIANDLOCATIONCHECKER.check(this, wm, lm);
    }

    @Override
    public void onWifiAndLocationEnabled() {
        openWifiDirectChannel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WIFIANDLOCATIONCHECKER.PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openWifiDirectChannel();
                else finishActivity(0);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == WIFIANDLOCATIONCHECKER.WIFI_OR_LOCATION_REQUEST) {
            WIFIANDLOCATIONCHECKER.check(this,wm,lm);
        }
    }

    private void openWifiDirectChannel(){
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        //Use reflection API to get the hidden method for setting the P2P network name
        try {
            Method m = manager.getClass().getMethod(
                    "setDeviceName",
                    WifiP2pManager.Channel.class, String.class, WifiP2pManager.ActionListener.class );
            m.invoke(manager, channel, name, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    //Code for Success in changing name
                }

                public void onFailure(int reason) {
                    //Code to be done while name change Fails
                }
            });
        } catch (Exception e) {

            e.printStackTrace();
        }

        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Device is ready to accept incoming connections from peers.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WifiDirectActivity.this, "P2P group creation failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void requestPeers(WifiP2pManager.PeerListListener listener){
        manager.requestPeers(channel,listener);
    }

    protected void discoverPeers(WifiP2pManager.ActionListener listener){
        manager.discoverPeers(channel, listener);
    }

    protected void requestConnectionInfo(){
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {

            }
        });
    }

    protected void setBroadcastReceiver(BroadcastReceiver broadcastReceiver){
        if(receiver!=null) {
            unregisterReceiver(receiver);
        }
        this.receiver = broadcastReceiver;
        registerReceiver(receiver,intentFilter);
    }

    protected void setGroupInfoListener(WifiP2pManager.GroupInfoListener groupInfoListener){
        this.groupInfoListener = groupInfoListener;
    }

    protected void requestGroupInfo(){
        manager.requestGroupInfo(channel,groupInfoListener);
    }


    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        if(receiver!=null) {
            registerReceiver(receiver, intentFilter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(receiver!=null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public void onStop(){
        if(manager!=null) {
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // Device is ready to accept incoming connections from peers.
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(WifiDirectActivity.this, "P2P group removal failed. Retry.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        super.onStop();
    }
}
