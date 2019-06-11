package edu.hm.eem_client.view;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import java.net.InetAddress;

import edu.hm.eem_client.R;
import edu.hm.eem_client.net.ClientProtocolManager;

public class LockedActivity extends AppCompatActivity {
    private ClientProtocolManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);
        Intent intent = getIntent();
        InetAddress host = (InetAddress) intent.getSerializableExtra(ScanActivity.ADDRESS_FIELD);
        int port = intent.getIntExtra(ScanActivity.PORT_FIELD, 0);
        TextView nameView = findViewById(R.id.itemname);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString(getString(R.string.preferences_username), "Username@" + android.os.Build.MODEL);
        pm = new ClientProtocolManager(this, host, port, name);
    }

    @Override
    protected void onStop() {
        super.onStop();
        pm.quit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
