package edu.hm.eem_host.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import edu.hm.eem_host.R;
import edu.hm.eem_library.net.ServiceManager;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        final EditText nameEditText = findViewById(R.id.username);
        findViewById(R.id.bt_save_prefs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = nameEditText.getText().toString();
                int full_size = ServiceManager.PROF_ATTRIBUTE_NAME.getBytes(US_ASCII).length + username.getBytes(UTF_8).length;
                // According to NsdServiceInfo.setAttribute(String, String), full attribute length should not exceed 255 bytes.
                if(full_size>=255){
                    Toast.makeText(SettingsActivity.this, R.string.toast_please_use_a_shorter_username, Toast.LENGTH_SHORT).show();
                } else {
                    editor.putString(getString(R.string.preferences_username), username);
                    editor.apply();
                }
            }
        });
    }
}
