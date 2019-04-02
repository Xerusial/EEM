package edu.hm.eem_host.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import edu.hm.eem_host.R;

public class LockActivity extends WifiDirectActivity {
    private TextView net_name;
    private TextView net_pw;
    private TextView con_devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        net_name = findViewById(R.id.net_name);
        net_pw = findViewById(R.id.net_pw);
        con_devices = findViewById(R.id.con_devices);
        setGroupInfoListener(new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if(group!=null) {
                    net_name.setText(group.getNetworkName());
                    net_pw.setText(group.getPassphrase());
                }
            }});
        final WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                con_devices.setText(String.valueOf(peerList.getDeviceList().size()));
            }
        };
        setBroadcastReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                    // Determine if Wifi P2P mode is enabled or not, alert
                    // the Activity.
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    } else {
                    }
                } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

                    requestPeers(peerListListener);

                } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

                    // Connection state changed! We should probably do something about
                    // that.

                } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {


                }
            }
        });
        findViewById(R.id.bt_req_wifi_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGroupInfo();
            }
        });

    }
}
