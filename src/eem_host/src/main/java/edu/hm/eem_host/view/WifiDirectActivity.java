package edu.hm.eem_host.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.lang.reflect.Method;
import edu.hm.eem_host.R;

public class WifiDirectActivity extends AppCompatActivity {
    private final String name = "Exammode";
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.GroupInfoListener groupInfoListener;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager manager;
    private BroadcastReceiver receiver;

    private final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION=1;
    private final int BACK_FROM_GPS_SETTINGS=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkGPSPermissions();

        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void checkGPSPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                Toast.makeText(getApplicationContext(),getString(R.string.toast_gps_neeeded_for_hotspot), Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        } else {
            openWifiDirectChannel();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openWifiDirectChannel();
                else finishActivity(0);
                break;
            }
        }
    }

    /*private void openGPSDialog(){
        if ( !((LocationManager)getApplicationContext().getSystemService( Context.LOCATION_SERVICE)).isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showAlertMessageNoGps();
        } else {
            openWifiDirectChannel();
        }
    }

    private void showAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.alert_gps_for_hotspot))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, final int id) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), BACK_FROM_GPS_SETTINGS);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        finishActivity(0);
                    }
                });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == BACK_FROM_GPS_SETTINGS) {
            openGPSDialog();
        }
    }*/

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
        super.onStop();
    }
}
