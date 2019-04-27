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
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.nsd.NsdManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import edu.hm.eem_client.net.ClientProtocolManager;
import edu.hm.eem_library.WIFIANDLOCATIONCHECKER;
import edu.hm.eem_library.net.ProtocolManager;

public class MainActivity extends AppCompatActivity {
    private NsdManager nsdm;
    private ConnectivityManager cm;
    private ClientProtocolManager clientProtocolManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nsdm = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        cm = (ConnectivityManager) getApplicationContext().getSystemService( Context.CONNECTIVITY_SERVICE);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanNetwork();
                v.setClickable(false);
            }
        });
        clientProtocolManager = new ClientProtocolManager(nsdm);
    }

    public void scanNetwork(){
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if((activeNetwork != null) && (activeNetwork.isConnected())){

            } else
                Toast.makeText(this, "Network is not up yet!", Toast.LENGTH_SHORT).show();
        }
    }
}
