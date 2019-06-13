package edu.hm.eem_client.view;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;

import edu.hm.eem_client.R;
import edu.hm.eem_client.net.ClientProtocolManager;
import edu.hm.eem_library.net.ProtocolHandler;

public class LockedActivity extends AppCompatActivity {
    private ClientProtocolManager pm;
    private LockedHandler handler;
    private ImageView lightHouse;

    public class LockedHandler extends Handler {
        private LockedHandler(Looper looper){
            super(looper);
        }

        public void postLighthouse(boolean on){
            this.post(() -> lightHouse.setVisibility(on?View.VISIBLE:View.INVISIBLE));
        }

        public void gracefulShutdown(){
            this.post(() ->{
                Log.e(LockedActivity.this.getPackageName(), "Connection was refused!");
                finish();
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);
        Intent intent = getIntent();
        InetAddress host = (InetAddress) intent.getSerializableExtra(ScanActivity.ADDRESS_FIELD);
        int port = intent.getIntExtra(ScanActivity.PORT_FIELD, 0);
        TextView nameView = findViewById(R.id.textView);
        nameView.setText(intent.getStringExtra(ScanActivity.PROF_FIELD));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString(getString(R.string.preferences_username), "Username@" + android.os.Build.MODEL);
        lightHouse = findViewById(R.id.lighthouse);
        handler = new LockedHandler(Looper.getMainLooper());
        pm = new ClientProtocolManager(this, host, port, name, handler);
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
