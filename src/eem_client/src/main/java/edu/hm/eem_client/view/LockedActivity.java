package edu.hm.eem_client.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.net.InetAddress;

import edu.hm.eem_client.R;
import edu.hm.eem_client.net.ClientProtocolManager;
import edu.hm.eem_client.net.ClientServiceManager;

public class LockedActivity extends AppCompatActivity {
    private ClientProtocolManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);
        Intent intent = getIntent();
        InetAddress host = (InetAddress) intent.getSerializableExtra(ClientServiceManager.ADDRESS_FIELD);
        int port = intent.getIntExtra(ClientServiceManager.PORT_FIELD, 0);
        getPreferences(Context.MODE_PRIVATE);
        String name = getResources().getString(R.string.preferences_username);
        TextView nameView = findViewById(R.id.textView);
        pm = new ClientProtocolManager(this, host, port, name, nameView);
    }
}
