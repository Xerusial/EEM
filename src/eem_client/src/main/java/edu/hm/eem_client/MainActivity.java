package edu.hm.eem_client;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import edu.hm.eem_client.net.ClientProtocolManager;

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
        findViewById(R.id.bt_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
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
